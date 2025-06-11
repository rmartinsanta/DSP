package es.urjc.etsii.grafo.PDSP.model;

import java.util.Objects;

import static es.urjc.etsii.grafo.PDSP.model.PDSPSolution.INFEASIBLE_SCORE;

public class AddMove extends PDSPBaseMove {

    /**
     * Move constructor with efficient and non-efficient options
     *
     * @param solution solution
     */
    public AddMove(PDSPSolution solution, int nodeId, boolean efficient) {
        super(solution,nodeId);
        // No cost is calculated for the efficient case

        if (!efficient) {
            // Obtain the movement cost (inefficiently)
            var s = new PDSPSolution(solution);
            this._execute(s);
            if (s.getScore() >= INFEASIBLE_SCORE) {
                // Infeasible solution
                this.moveValue = s.getScore();
            } else {
                // Regular add case
                this.moveValue = 1;
            }
        }
    }


    @Override
    protected PDSPSolution _execute(PDSPSolution solution) {
        solution.addNode(nodeId,this.solutionVersion);
        return solution;
    }

    @Override
    public double getValue() {
        return 1;
    }

    /**
     * Equals only considers the node id and the class type, not the move value because it
     * could be different in different solutions.
     *
     * @return true if the node id is the same and the class is the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddMove addMove = (AddMove) o;

        return nodeId == addMove.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, this.getClass());
    }


    @Override
    public String toString() {
        return "AddMove{" +
                "nodeId=" + nodeId +
                ", moveValue=" + moveValue +
                ", moveValueConsideringNeighbors=" + moveValueConsideringNeighbors +
                '}';
    }

}
