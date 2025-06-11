package es.urjc.etsii.grafo.PDSP.constructives.grasp;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.create.grasp.GreedyRandomGRASPConstructive;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class CSharpGRGrasp extends GreedyRandomGRASPConstructive<PDSPGRASPMove, PDSPSolution, PDSPInstance> {

    public CSharpGRGrasp(double alpha, GRASPListManager<PDSPGRASPMove, PDSPSolution, PDSPInstance> candidateListManager, ToDoubleFunction<PDSPGRASPMove> greedyFunction) {
        super(
                FMode.MAXIMIZE,
                candidateListManager,
                greedyFunction,
                alpha == -1? () -> RandomManager.getRandom().nextDouble(): () -> alpha,
                alpha == -1? "RND": "FIXED{%s}".formatted(alpha)
        );
    }


    @Override
    protected int getCandidateIndex(double alpha, List<PDSPGRASPMove> cl) {
        var r = RandomManager.getRandom();
        int bestIndex = -1;
        double bestValue = -Double.MAX_VALUE;
        for (int i = 0, clSize = cl.size(); i < clSize; i++) {
            PDSPGRASPMove move = cl.get(i);
            double cValue = greedyFunction.applyAsDouble(move) * (1 + alpha * r.nextDouble());
            if (fmode.isBetter(cValue, bestValue)) {
                bestIndex = i;
                bestValue = cValue;
            }
        }
        assert bestIndex != -1 && bestValue != -Double.MAX_VALUE;
        return bestIndex;
    }
}

