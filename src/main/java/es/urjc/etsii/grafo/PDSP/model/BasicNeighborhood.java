package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.ArrayList;

public class BasicNeighborhood extends Neighborhood<PDSPBaseMove, PDSPSolution, PDSPInstance> {

    @Override
    public ExploreResult<PDSPBaseMove, PDSPSolution, PDSPInstance> explore(PDSPSolution solution) {
        var list = new ArrayList<PDSPBaseMove>();

        // Drop moves: only drop nodes that are not support nodes
        for (var node : solution.getChosen())
            if (!solution.getInstance().getSupportNodes().contains(node))
                list.add(new DropMove(solution, node.intValue(),false));

        // Add moves: only add those that are not leafs
        for (var node : solution.getAddableNodes())
            if (!solution.getInstance().isLeaf(node))
                list.add(new AddMove(solution, node.intValue(),false));

        return ExploreResult.fromList(list);
    }
}
