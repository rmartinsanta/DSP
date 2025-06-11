package es.urjc.etsii.grafo.PDSP.algorithm;

import es.urjc.etsii.grafo.PDSP.model.*;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.metrics.BestObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tabu search algorithm. This implementation has six main components:
 * - A constructive algorithm to generate the initial solution (mandatory)
 * - A neighborhood to explore (mandatory)
 * - An attribute-based short-term memory
 * - A frequency-based long-term memory
 * - Strategic oscillation criteria
 * - Path relinking component
 * <p>
 * The last three components can be either activated or deactivated, while
 * the short-term memory is always active.
 */
public class TabuSearchAlgorithm extends Algorithm<PDSPSolution, PDSPInstance> {

    private static final long MIN_ITERATIONS = 100;
    private static final int SO_MIN_ITERATIONS = 2;
    private final double maxIterationsRate;
    private final Constructive<PDSPSolution, PDSPInstance> constructive;
    private final Neighborhood<PDSPBaseMove, PDSPSolution, PDSPInstance> neighborhoodToExplore;

    // Tabu scope is used to bind the tabu list to the current thread
    private final ScopedValue<TabuStorage<Integer>> tabuScope = ScopedValue.newInstance();

    private final double tabuTenure;
    private final boolean useLongTermMemory;
    private final double strategicOscillationRate;
    private int tabuTenureIterations;

    private static final Logger log = LoggerFactory.getLogger(TabuSearchAlgorithm.class);

    /**
     * Initialize common algorithm fields
     *
     * @param algorithmName algorithm name
     */
    public TabuSearchAlgorithm(String algorithmName, double maxIterationsRate, double tabuTenure, Constructive<PDSPSolution, PDSPInstance> constructive, Neighborhood<PDSPBaseMove, PDSPSolution, PDSPInstance> neighborhoodToExplore, boolean useLongTermMemory, double strategicOscillationRate) {
        super(algorithmName);

        this.maxIterationsRate = maxIterationsRate;
        this.constructive = constructive;
        this.neighborhoodToExplore = neighborhoodToExplore;
        this.tabuTenure = tabuTenure;
        // Iterations must be initialized with the instance
        this.tabuTenureIterations = -1;
        this.useLongTermMemory = useLongTermMemory;
        this.strategicOscillationRate = strategicOscillationRate;
    }

    @Override
    public PDSPSolution algorithm(PDSPInstance instance) {
        // Initialize tabu tenure iterations
        tabuTenureIterations = Math.max(1, (int) (instance.nNodes() * tabuTenure));
        // Build tabu list for nodes
        TabuStorage<Integer> tabu = new TabuStorage<>(tabuTenureIterations);

        // Bind tabu to the current thread and execute algorithm.
        // This way each thread has its own independent tabu storage, no synchronization is needed
        return ScopedValue.getWhere(tabuScope, tabu, () -> algorithmWithTabu(instance));
    }

    public PDSPSolution algorithmWithTabu(PDSPInstance instance) {

        // Initial solution
        PDSPSolution best = constructive.construct(new PDSPSolution(instance));
        Metrics.add(BestObjective.class, best.getScore());
        PDSPSolution current = new PDSPSolution(best);

        // Tabu components
        TabuStorage<Integer> tabu = tabuScope.get();

        // Main loop
        int iterations = 1;

        // Maximum iterations depends on the size of the instance, with a minimum of MIN_ITERATIONS
        long maxIterationsTentative = (long) (instance.nNodes() * maxIterationsRate);
        long maxIterations = MIN_ITERATIONS;
        if (maxIterationsTentative > maxIterations) {
            maxIterations = maxIterationsTentative;
        }

        // Strategic oscillation iterations depends on the number of iterations, with a minimum of SO_MIN_ITERATIONS
        int strategicOscillationIterations = 0;
        if (strategicOscillationRate > 0.0) {
            int strategicOscillationIterationsTentative = (int) (maxIterations * strategicOscillationRate);
            strategicOscillationIterations = SO_MIN_ITERATIONS;
            if (strategicOscillationIterationsTentative > strategicOscillationIterations) {
                strategicOscillationIterations = strategicOscillationIterationsTentative;
            }
        }
        int strategicOscillationIterationsRecall = strategicOscillationIterations;

        while (!TimeControl.isTimeUp()) {

            // Explore neighborhood and check with tabu list
            ArrayList<PDSPBaseMove> availableMoves = new ArrayList<>();
            tabu.checkExpiration(iterations);
            AtomicBoolean improvingMoves = new AtomicBoolean(false);
            // Next variable is final because it is used inside the lambda
            PDSPSolution _best = best;
            neighborhoodToExplore.explore(current).moves().forEach(move -> {
                // Aspiration criteria: if the move improves the best solution, it is always available
                if ((current.getScore() + move.getValue()) < _best.getScore()) {
                    availableMoves.add(move);
                    improvingMoves.set(true);
                } else {
                    // If a move is not improving the best solution, could be considered tabu.
                    if (!tabu.contains(move.getNodeId())) {
                        availableMoves.add(move);
                        if (move.getValue() < 0) improvingMoves.set(true);
                    }
                }
            });

            // Long term memory: only if no improving moves are found
            boolean applyLongTermMemory = (useLongTermMemory && !improvingMoves.get());

            // Strategic oscillation
            boolean allowUnfeasible = (strategicOscillationIterations > 0);

            // Get best move considering the secondary objective function
            PDSPBaseMove bestMove = CollectionUtil.getBest(availableMoves, pdspBaseMove -> pdspBaseMove.getValueConsideringNeighbors(applyLongTermMemory, allowUnfeasible), DoubleComparator::isLess);
            if (bestMove == null) {
                // No available moves, stop
                break;
            }

            // Apply move
            bestMove.execute(current);

            // Update tabu list with the node and current iteration.
            // If a node has been removed (drop) from the solution, tabuTenure is doubled
            if (bestMove.getClass().equals(DropMove.class) || bestMove.getClass().equals(EfficientDropMove.class)) {
                tabu.add(bestMove.getNodeId(), iterations, 2);
            } else {
                tabu.add(bestMove.getNodeId(), iterations, 1);
            }

            // Update best solution if is feasible and better
            if (current.isCovered() && current.isBetterThan(best)) {
                best = new PDSPSolution(current);
                Metrics.add(BestObjective.class, best.getScore());
            }

            // Update strategic oscillation iterations
            if (allowUnfeasible) {
                strategicOscillationIterations--;
            } else {
                // Restore strategic oscillation iterations if current solution is covered and
                // the same number of iterations occurred, represented as negative values.
                if (current.isCovered()) {
                    strategicOscillationIterations--;
                    if (strategicOscillationIterations <= (-1 * strategicOscillationIterationsRecall)) {
                        // Restore allowing unfeasible solutions.
                        strategicOscillationIterations = strategicOscillationIterationsRecall;
                    }
                }
            }

            // Check stop condition
            if (iterations++ == maxIterations) {
                break;
            }

            // Iteration; size; feasible; so enabled
            //log.debug("{};{};{};{}",iterations, current.getChosen().size(), current.isCovered(), allowUnfeasible);
            //log.debug("Iteration: {} - Current sol: {} - Cov.: {} - SO: {} - Best: {}", iterations, current, current.isCovered(), allowUnfeasible,best);

        }

        return best;
    }
}