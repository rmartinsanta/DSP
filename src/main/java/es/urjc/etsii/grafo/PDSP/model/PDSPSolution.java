package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.*;

public class PDSPSolution extends Solution<PDSPSolution, PDSPInstance> {

    private final BitSet chosen;
    private final BitSet unobservedNodes;

    private final HashMap<Integer, Integer> dominatedNodes;

    public static final double INFEASIBLE_SCORE = Double.POSITIVE_INFINITY;

    private double score = INFEASIBLE_SCORE;

    private long[] lastIterationInSolution;

    private long[] lastIterationOutOfTheSolution;

    /**
     * Initialize solution from instance
     *
     * @param instance  instance to initialize from
     */
    public PDSPSolution(PDSPInstance instance, boolean forceEmpty) {
        super(instance);
        this.unobservedNodes = new BitSet(instance.nNodes());
        // Create nodes and unmark all of them
        for (var e : instance.graph().entrySet()) {
            int id = e.getKey();
            unobservedNodes.add(id);
        }
        dominatedNodes = HashMap.newHashMap(instance.nNodes());
        for (int i = 0; i < instance.nNodes(); i++) {
            dominatedNodes.put(i, 0);
        }

        this.chosen = new BitSet(instance.nNodes());

        // Add one extra node for because instances may have nodes from 0 to n-1 or from 1 to n
        lastIterationInSolution = new long[instance.nNodes()];
        lastIterationOutOfTheSolution = new long[instance.nNodes()];

        // Add reference solution if required
        if (!forceEmpty) {
            var refSol = instance.referenceSolution();
            for (var n : refSol.chosen.stream().toList()) {
                addNode(n);
            }
        }
    }

    public HashMap<Integer, Integer> getDominatedNodes() {
        return dominatedNodes;
    }

    public BitSet getUnobservedNodes() {
        return unobservedNodes;
    }

    /**
     * Initialize solution from instance
     *
     * @param instance instance to initialize from
     */
    public PDSPSolution(PDSPInstance instance) {
        this(instance, false);
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public PDSPSolution(PDSPSolution s) {
        super(s);
        this.chosen = new BitSet(s.chosen);
        this.unobservedNodes = new BitSet(s.unobservedNodes);
        this.dominatedNodes = new HashMap<>(s.dominatedNodes);
        this.score = s.score;
        this.lastIterationInSolution = Arrays.copyOf(s.lastIterationInSolution, s.lastIterationInSolution.length);
        this.lastIterationOutOfTheSolution = Arrays.copyOf(s.lastIterationOutOfTheSolution, s.lastIterationOutOfTheSolution.length);
    }


    @Override
    public PDSPSolution cloneSolution() {
        // You do not need to modify this method
        // Call clone constructor
        return new PDSPSolution(this);
    }

    @Override
    protected boolean _isBetterThan(PDSPSolution other) {
        return this.chosen.size() < other.chosen.size();
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    @Override
    public double getScore() {
        return score;
    }

    /**
     * Recalculate solution score from scratch, using the problem objective function.
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * This method will be used to validate the correct behaviour of the getScore() method, and to help catch
     * bugs or mistakes when changing incremental score calculation.
     *
     * @return current solution score as double
     */
    @Override
    public double recalculateScore() {
        PDSPSolution newSol = new PDSPSolution(this.getInstance(), true);
        for (int i : this.chosen.stream().toList()) {
            newSol.addNode(i);
        }

        return newSol.getScore();
    }

    public BitSet getChosen() {
        return chosen;
    }


    /**
     * Remove the given node from the solution and updates cached score. The resulting solution MAY BE FEASIBLE
     * if strategic oscillation is active.
     *
     * @param nodeId node to remove
     */
    public void removeNode(int nodeId) {
        assert this.chosen.contains(nodeId);
        assert getInstance().configurableNodes().contains(nodeId);

        this.chosen.remove(nodeId);

        // If current node is not observed by a neighbor, add it to unobserve set
        int observedBy = dominatedNodes.get(nodeId) - 1;
        dominatedNodes.put(nodeId, observedBy);
        if (observedBy == 0) {
            unobservedNodes.add(nodeId);
        }

        // Decrease the number of nodes that observe the neighbors of nodeId
        for (Integer neigh : getNeighbors(nodeId)) {
            int neighObservedBy = dominatedNodes.get(neigh) - 1;
            dominatedNodes.put(neigh, neighObservedBy);
            if (neighObservedBy == 0) {
                unobservedNodes.add(neigh);
            }
        }
        this.score = computeScore(this);
        notifyUpdate();
    }

    /**
     * Checks if the solution is still feasible if the given node is removed.
     * @param node node to try to remove
     * @return false if the solution is unfeasible after removing the node, true otherwise
     * Note that this method does not care about strategic oscillation
     */
    public boolean isCoveredIfRemoves(int node){
        assert chosen.contains(node);
        assert getInstance().configurableNodes().contains(node);

        if(dominatedNodes.get(node) == 1){
            // Current node is observed by itself
            return false;
        }

        for (Integer neigh : getNeighbors(node)) {
            if(dominatedNodes.get(neigh) == 1){
                // Neighbor is observed by this node, if removed it will stop being observed
                return false;
            }
        }
        return true;
    }

    /**
     * Updates iterations regarding the node and removes it from the solution.
     *
     * @param nodeId node to remove
     * @param currentIteration current iteration
     */
    public void removeNode(int nodeId, long currentIteration) {
        // Reset last iteration in solution
        lastIterationInSolution[nodeId] = 0;
        // Set last iteration out of the solution
        lastIterationOutOfTheSolution[nodeId] = currentIteration;
        removeNode(nodeId);
    }

    /**
     * Are we covering all nodes?
     *
     * @return true if all nodes are observed
     */
    public boolean isCovered() {
        return this.unobservedNodes.isEmpty();
    }

    /**
     * Pick and include node n in solution updating cached score.
     *
     * @param n Node id to add
     */
    public void addNode(int n, long currentIteration) {
        // Update last iteration in solution
        lastIterationInSolution[n] = currentIteration;
        // Reset last iteration out of the solution
        lastIterationOutOfTheSolution[n] = 0;
        addNode(n);
    }

    /**
     * Pick and include node n in solution updating cached score.
     *
     * @param nodeId Node id to add
     */
    public void addNode(int nodeId) {
        assert !this.chosen.contains(nodeId);

        this.chosen.add(nodeId);
        this.unobservedNodes.remove(nodeId);
        dominatedNodes.put(nodeId, dominatedNodes.get(nodeId) + 1);

        // Increase the number of nodes that observe the neighbors of nodeId
        for (Integer neigh : getNeighbors(nodeId)) {
            dominatedNodes.put(neigh, dominatedNodes.get(neigh) + 1);
            unobservedNodes.remove(neigh);
        }
        this.score = computeScore(this);
        notifyUpdate();
    }

    /**
     * Compute the score of a given solution
     *
     * @param s solution to compute score
     * @return score of the solution
     */
    private double computeScore(PDSPSolution s) {
        return computeScore(s.chosen.size(), s.unobservedNodes.size());
    }

    /**
     * Compute the score of a solution with the given parameters
     *
     * @param chosenSize     number of nodes in the solution
     * @param unobservedSize number of nodes not observed by any node in the solution
     * @return score of the solution
     */
    public static double computeScore(int chosenSize, int unobservedSize) {
        if (unobservedSize == 0) {
            return chosenSize;
        } else {
            return INFEASIBLE_SCORE;
        }
    }


    public BitSet getNeighbors(int nodeId) {
        return getInstance().graph().get(nodeId);
    }

    /**
     * Get all nodes that can be added to the solution
     *
     * @return set with all nodes not already in the solution and not excluded
     */
    public BitSet getAddableNodes() {
        return BitSet.difference(this.getInstance().configurableNodes(), this.chosen);
    }

    /**
     * Get all nodes that can be potentially removed from the solution
     *
     * @return set with all nodes in the solution that have not been locked
     */
    public BitSet getRemovableNodes() {
        return BitSet.intersection(this.getInstance().configurableNodes(), this.chosen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDSPSolution solution = (PDSPSolution) o;
        return Objects.equals(chosen, solution.chosen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chosen);
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        return this.chosen.size() + " -> " + this.chosen;
    }

    public long getPreviousIterationInSolution(int nodeId) {
        return lastIterationInSolution[nodeId];
    }

    public long getPreviousIterationOutOfTheSolution(int nodeId) {
        return lastIterationOutOfTheSolution[nodeId];
    }

    /**
     * Obtains the number of nodes adjacent to nodeId that are not dominated by any other node in the solution.
     * @param nodeId
     * @return number of nodes adjacent to nodeId that are not dominated by any other node in the solution.
     */
    public long getAdjacentNodesNotDominated(int nodeId) {
        long num = 0;
        for (int n : getNeighbors(nodeId)) {
            // The if considers the case when the adjacent node is not in the solution
            if (!chosen.get(n) && (dominatedNodes.get(n) == null || dominatedNodes.get(n) == 1)) {
                num++;
            }
        }
        return num;
    }

    /**
     * Calculates the sum of the dominating nodes of the neighbors of the given node.
     * @param nodeId
     * @return sum of the dominating nodes of the neighbors of the given node.
     */
    public long getSumOfDominatingNodes(int nodeId) {
        long sum = 0L;
        for (Integer n : getNeighbors(nodeId)) {
            long orDefault = dominatedNodes.getOrDefault(n, 0);
            sum += orDefault;
        }
        return sum;
    }
}
