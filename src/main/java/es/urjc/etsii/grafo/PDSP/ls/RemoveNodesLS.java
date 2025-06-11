package es.urjc.etsii.grafo.PDSP.ls;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.BestObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveNodesLS extends Improver<PDSPSolution, PDSPInstance> {

    private static final Logger log = LoggerFactory.getLogger(RemoveNodesLS.class);

    public RemoveNodesLS() {
        super(FMode.MINIMIZE);
    }

    @Override
    protected PDSPSolution _improve(PDSPSolution solution) {
        for(var node: solution.getRemovableNodes()){
            if(solution.isCoveredIfRemoves(node)){
                log.debug("Removing node {}", node);
                solution.removeNode(node);
                Metrics.add(BestObjective.class, solution.getScore());
            }
        }

        return solution;
    }
}
