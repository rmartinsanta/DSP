package es.urjc.etsii.grafo.PDSP.constructives.grasp;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.solution.EagerMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PDSPGRASPMove extends EagerMove<PDSPSolution, PDSPInstance> {

    private static final Logger log = LoggerFactory.getLogger(PDSPGRASPMove.class);
    public static final int NOT_EVALUATED = -1;
    private final int nodeId;
    private final PDSPSolution solution;
    private int howManyObserves;

    public PDSPGRASPMove(PDSPSolution solution, int nodeId) {
        super(solution);
        this.nodeId = nodeId;
        this.howManyObserves = NOT_EVALUATED;
        this.solution = solution;
    }

    private int calculateHowManyObserves(PDSPSolution solution, int nodeId) {
        int howManyMarks = 0;
        var unobserved = solution.getUnobservedNodes();
        for(Integer neigh: solution.getNeighbors(nodeId)){
            if(unobserved.contains(neigh)){
                howManyMarks++;
            }
        }
        return howManyMarks;
    }

    public int getNodeId() {
        return nodeId;
    }

    public double getHowManyObserves(){
        // Lazy evaluation for performance, only calculated if requested by algorithm
        if(howManyObserves == NOT_EVALUATED){
            howManyObserves = calculateHowManyObserves(solution, nodeId);
        }
        return howManyObserves;
    }


    @Override
    protected PDSPSolution _execute(PDSPSolution solution) {
        //log.info("Added \t{} \tscore {}", this.nodeId, this.howManyMarks);
        solution.addNode(this.nodeId);
        return solution;
    }

    @Override
    public double getValue() {
        return 1; // Each time we add an element the objective function increases by one, we want to minimize it
    }

    @Override
    public String toString() {
        return "Add{" +
                "id=" + nodeId +
                //", f=" + this.howManyMarks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDSPGRASPMove that = (PDSPGRASPMove) o;
        return nodeId == that.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
