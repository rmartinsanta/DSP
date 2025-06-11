package es.urjc.etsii.grafo.PDSP.model;

import static es.urjc.etsii.grafo.PDSP.model.PDSPSolution.INFEASIBLE_SCORE;

public class EfficientDropMove extends DropMove {

    double inverseMoveValue;

    /**
     * Move constructor
     *
     * @param solution solution
     * @param nodeId  node to drop
     */
    public EfficientDropMove(PDSPSolution solution, int nodeId) {
        super(solution, nodeId, true);

        // Obtains the movement cost (efficiently)
        int unobservedNodesSize = solution.getUnobservedNodes().size();

        // Consider the node itself
        if (solution.getDominatedNodes().get(nodeId) == 1) {
            unobservedNodesSize++;
        }

        // If still is dominated, consider neighbors
        if (unobservedNodesSize == 0) {
            // Update dominance of neighbor nodes
            for (int neighbor : solution.getNeighbors(nodeId)) {
                if (solution.getDominatedNodes().containsKey(neighbor)) {
                    // Adds the node to the unobserved if it has only one dominant (nodeId)
                    if (solution.getDominatedNodes().get(neighbor) == 1) {
                        unobservedNodesSize++;
                    }
                }
            }
        }

        double newScore = PDSPSolution.computeScore(solution.getChosen().size() - 1, unobservedNodesSize);

        if (newScore >= INFEASIBLE_SCORE) {
            // Infeasible solution
            this.moveValue = newScore;
        } else {
            // Regular drop case
            this.moveValue = -1;
        }

    }

    public String toString() {
        return "EfficientDropMove{" +
                "nodeId=" + nodeId +
                ", moveValue=" + moveValue +
                ", moveValueConsideringNeighbors=" + moveValueConsideringNeighbors +
                ", originalMoveValue=" + moveValueConsideringNeighborsOriginal +
                '}';
    }

}
