package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Base class for all movements for the PDSP problem. All movements should extend this class.
 */
public abstract class PDSPBaseMove extends Move<PDSPSolution, PDSPInstance> {

    protected double moveValue;
    protected int nodeId;
    protected double numberOfNeighbours;
    protected int numberOfNodes;
    protected double moveValueConsideringNeighbors;
    protected long previousIterationInSolution;
    protected double moveValueConsideringNeighborsOriginal;
    protected long previousIterationOutOfTheSolution;
    protected long adjacentNodesNotDominated;
    protected long sumOfDominatingNodes;

    /**
     * Move constructor needed for efficient moves.
     * @param solution solution
     */
    public PDSPBaseMove(PDSPSolution solution) {
        super(solution);
    }

    /**
     * Move constructor
     * @param solution solution
     */
    public PDSPBaseMove(PDSPSolution solution, int nodeId) {
        super(solution);
        this.nodeId = nodeId;
        this.numberOfNodes = solution.getInstance().nNodes();
        this.numberOfNeighbours = solution.getNeighbors(nodeId).size();
        this.previousIterationInSolution = solution.getPreviousIterationInSolution(nodeId);
        this.previousIterationOutOfTheSolution = solution.getPreviousIterationOutOfTheSolution(nodeId);
        this.adjacentNodesNotDominated = solution.getAdjacentNodesNotDominated(nodeId);
        this.sumOfDominatingNodes = solution.getSumOfDominatingNodes(nodeId);
    }

    /**
     * Executes the proposed move,
     * to be implemented by each move type.
     * @param solution Solution where this move will be applied to.
     * @return true if the solution has changed,
     * false if for any reason the movement is not applied or the solution does not change after executing the move
     */
    @Override
    protected abstract PDSPSolution _execute(PDSPSolution solution);

    /**
     * Get the movement value, represents how much does the move changes the f.o of a solution if executed
     *
     * @return f.o change
     */
    public double getValue() {
        return moveValue;
    }


    /**
     * Get value of the move considering a secondary function:
     * Add move: the higher the number of non-dominated neighbors, the better.
     * Drop move: the higher the domination of neighbors, the better.
     * Drop moves must be better than add moves.
     *
     * @return move value considering the number of neighbor nodes
     */
    public double getValueConsideringNeighbors(boolean useLongTermMemory, boolean allowInfeasible) {

        boolean isAddMove = this.getClass().equals(AddMove.class) || this.getClass().equals(EfficientAddMove.class);

        if (isAddMove) {
            // In an ADD move, the higher the number of non-dominated neighbors, the better.
            // Since we minimize, we need to invert the value keeping it positive.
            moveValueConsideringNeighbors = this.numberOfNodes - this.adjacentNodesNotDominated;
            if (allowInfeasible && (this.moveValue == 1)) {
                // If the move is reaching feasibility, we get its original value (+1)
                // since we prefer to reach feasibility
                moveValueConsideringNeighbors = this.moveValue;
            }
        } else {
            // Drop moves
            if ((this.moveValue == -1) || allowInfeasible) {
                // In a valid drop move or while using SO, the higher the domination of neighbors, the better.
                // Since we minimize, we need to invert the value, making it negative.
                moveValueConsideringNeighbors = -1.0 * this.sumOfDominatingNodes;
            } else {
                // Invalid drop move (unfeasible)
                moveValueConsideringNeighbors = this.moveValue;
            }
        }

        // Value to recall in debug
        moveValueConsideringNeighborsOriginal = moveValueConsideringNeighbors;

        // Long term memory applies when no improving moves are found, so only ADD moves remain
        if (useLongTermMemory && isAddMove) {
            // The number of iterations of the node NOT in the solution is used to reduce the long term memory value
            long incentive = (this.solutionVersion - this.previousIterationOutOfTheSolution);
            moveValueConsideringNeighbors -= incentive;
        }

        return moveValueConsideringNeighbors;

    }

    /**
     * Returns a String representation of the current movement. Only use relevant fields.
     * Tip: Default IntelliJ implementation is fine
     *
     * @return human readable string
     */
    public abstract String toString();

    /** {@inheritDoc} */
    @Override
    public abstract boolean equals(Object o);

    /** {@inheritDoc} */
    @Override
    public abstract int hashCode();

//    /**
//     * Returns the inverse move of this move.
//     * @param solution solution where this move was applied
//     * @return inverse move
//     */
//    public abstract PDSPBaseMove getInverseMove(PDSPSolution solution);
//
//    public void setValue(double moveValue) {
//        this.moveValue = moveValue;
//    }

    /**
     * Get the node id of the move
     * @return node id
     */
    public int getNodeId() {
        return nodeId;
    }

}
