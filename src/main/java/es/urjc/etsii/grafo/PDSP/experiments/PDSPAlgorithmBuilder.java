package es.urjc.etsii.grafo.PDSP.experiments;

import es.urjc.etsii.grafo.PDSP.algorithm.TabuSearchAlgorithm;
import es.urjc.etsii.grafo.PDSP.constructives.PDSPRandomConstructive;
import es.urjc.etsii.grafo.PDSP.model.EfficientBasicNeighborhood;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;

public class PDSPAlgorithmBuilder extends AlgorithmBuilder<PDSPSolution, PDSPInstance> {
    @Override
    public Algorithm<PDSPSolution, PDSPInstance> buildFromConfig(AlgorithmConfiguration config) {
        int maxIterations = 1000;

        double tabuTenure = Double.parseDouble(config.getValue("tabuTenure").orElse("-1"));
        boolean useLongTermMemory = Boolean.parseBoolean(config.getValue("useLongTermMemory").orElse("false"));
        boolean useStrategicOscillation = Boolean.parseBoolean(config.getValue("useStrategicOscillation").orElse("false"));
        int strategicOscillationLength = Integer.parseInt(config.getValue("strategicOscillationLength").orElse("0"));
        if (!useStrategicOscillation) {
            strategicOscillationLength = 0;
        }

        return new TabuSearchAlgorithm("TabuSearch", maxIterations, tabuTenure, new PDSPRandomConstructive(),new EfficientBasicNeighborhood(),useLongTermMemory, strategicOscillationLength);
    }
}
