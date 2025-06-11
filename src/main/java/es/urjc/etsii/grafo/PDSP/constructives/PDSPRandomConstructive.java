package es.urjc.etsii.grafo.PDSP.constructives;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.Reconstructive;

import static es.urjc.etsii.grafo.util.CollectionUtil.pickRandom;

public class PDSPRandomConstructive extends Reconstructive<PDSPSolution, PDSPInstance> {


    @Override
    public PDSPSolution construct(PDSPSolution solution) {
        // The solution is already initialized with the support nodes
        // We only need to add the rest of the nodes to dominate the rest of the graph
        while(!solution.isCovered()){
            var n = pickRandom(solution.getUnobservedNodes());
            solution.addNode(n);
        }

        return solution;
    }

    @Override
    public PDSPSolution reconstruct(PDSPSolution solution) {
        // In DSP reconstruct behaves the same as a normal construction
        return construct(solution);
    }
}
