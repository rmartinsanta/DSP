package es.urjc.etsii.grafo.PDSP.model;

import static es.urjc.etsii.grafo.PDSP.model.PDSPSolution.INFEASIBLE_SCORE;

/**
 * Move that adds a node to the solution computing the cost of the movement
 * more efficiently than the default implementation.
 */
public class EfficientAddMove extends AddMove {

    double inverseMoveValue;

    /**
     * Move constructor
     *
     * @param solution solution
     * @param nodeId node to add
     */
    public EfficientAddMove(PDSPSolution solution, int nodeId) {
        super(solution, nodeId, true);

        if (solution.getUnobservedNodes().isEmpty()) {
            // If all nodes are already dominated, the movement cost is one
            this.moveValue = 1;
        } else {
            // Obtain the movement cost (efficiently)
            int unobservedNodesSize = solution.getUnobservedNodes().size();

            // Update dominance of neighbor nodes
            for (int neighbor : solution.getNeighbors(nodeId)) {
                if (solution.getDominatedNodes().get(neighbor) == 0) {
                    assert solution.getUnobservedNodes().contains(neighbor);
                    unobservedNodesSize--;
                }
            }

            // Consider the node itself
            if (solution.getDominatedNodes().get(nodeId) == 0) {
                unobservedNodesSize--;
            }

            double newScore = PDSPSolution.computeScore(solution.getChosen().size() + 1, unobservedNodesSize);

            if (newScore >= INFEASIBLE_SCORE) {
                // Infeasible solution
                this.moveValue = newScore;
            } else {
                // Regular add case
                this.moveValue = 1;
            }
        }
    }


    public String toString() {
        return "EfficientAddMove{" +
                "nodeId=" + nodeId +
                ", moveValue=" + moveValue +
                ", moveValueConsideringNeighbors=" + moveValueConsideringNeighbors +
                ", originalMoveValue=" + moveValueConsideringNeighborsOriginal +
                '}';
    }
}
