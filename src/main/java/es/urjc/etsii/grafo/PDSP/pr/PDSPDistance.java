package es.urjc.etsii.grafo.PDSP.pr;

import com.google.common.collect.Sets;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionDistance;

public class PDSPDistance extends SolutionDistance<PDSPSolution, PDSPInstance> {

    @Override
    public double distances(PDSPSolution sa, PDSPSolution sb) {
        var setA = sa.getChosen();
        var setB = sb.getChosen();
        return Sets.symmetricDifference(setA, setB).size();
    }
}
