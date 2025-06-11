package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.ArrayList;

public class EfficientBasicNeighborhood extends Neighborhood<PDSPBaseMove, PDSPSolution, PDSPInstance> {

    @Override
    public ExploreResult<PDSPBaseMove, PDSPSolution, PDSPInstance> explore(PDSPSolution solution) {
        var list = new ArrayList<PDSPBaseMove>();
        var instance = solution.getInstance();


        // Drop moves: only drop nodes that are not support nodes
        var supportNodes = instance.getSupportNodes();
        for (var node : solution.getChosen()) {
            if (!supportNodes.contains(node)) {
                list.add(new EfficientDropMove(solution, node));
            }
        }

        // Add moves: only add those that are not leafs
        for (var node : solution.getAddableNodes()) {
            if (instance.isLeaf(node)) {
                throw new IllegalArgumentException("Node " + node + " is a leaf node, it should not be in the addable nodes list");
            }
            list.add(new EfficientAddMove(solution, node));
        }

        return ExploreResult.fromList(list);
    }
}
