package es.urjc.etsii.grafo.PDSP.experiments;

import es.urjc.etsii.grafo.PDSP.algorithm.TabuSearchAlgorithm;
import es.urjc.etsii.grafo.PDSP.constructives.PDSPRandomConstructive;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.PDSPGRASPMove;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.SmartPDSPListManager;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.*;
import es.urjc.etsii.grafo.PDSP.pr.PathRelinking;
import es.urjc.etsii.grafo.PDSP.pr.TabuRelinkedAlgorithm;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;


public class TabuSearchExperiment extends AbstractExperiment<PDSPSolution, PDSPInstance> {

    private final PDSPConfig config;

    public TabuSearchExperiment(PDSPConfig config) {
        // Any config class can be requested via the constructor
        this.config = config;
    }

    @Override
    public List<Algorithm<PDSPSolution, PDSPInstance>> getAlgorithms() {

        var algorithms = new ArrayList<Algorithm<PDSPSolution, PDSPInstance>>();
        var randomConstructive = new PDSPRandomConstructive();
        var greedyReconstruction = new GraspBuilder<PDSPGRASPMove, PDSPSolution, PDSPInstance>()
                .withGreedyFunction(PDSPGRASPMove::getHowManyObserves)
                .withListManager(new SmartPDSPListManager(false))
                .withMode(FMode.MAXIMIZE)
                .withStrategyGreedyRandom()
                .withAlphaValue(0)
                .build();
        var pathRelinking = new PathRelinking(greedyReconstruction, new RemoveNodesLS());

        double maxIterationsRate = config.getMaxIterationsRate();
        double tabuTenure = config.getTabuTenure();
        boolean useLongTermMemory = config.isUseLongTermMemory();
        boolean useStrategicOscillation = config.isUseStrategicOscillation();
        double strategicOscillationRate = 0.0;
        if (useStrategicOscillation) {
            strategicOscillationRate = config.getStrategicOscillationRate();
        }

        // Tabu search algorithm to be used as a constructor for PR
        var tsAlgorithm = new TabuSearchAlgorithm("TS_Eff_LTM_off_SO_off", maxIterationsRate, tabuTenure, randomConstructive,new EfficientBasicNeighborhood(),false, 0);
        algorithms.add(new TabuRelinkedAlgorithm("TS_PR", 10, 10, pathRelinking, tsAlgorithm, randomConstructive));

        // Non-efficient version of TS (basic neighborhood)
        // algorithms.add(new TabuSearchAlgorithm("TS_LTM_off_SO_off", maxIterationsRate, tabuTenure, new PDSPRandomConstructive(),new BasicNeighborhood(),false, 0));

        // Regular TS
        algorithms.add(new TabuSearchAlgorithm("TS_Eff_LTM_off_SO_off", maxIterationsRate, tabuTenure, new PDSPRandomConstructive(),new EfficientBasicNeighborhood(),false, 0));

        // TS with Long Term Memory (LTM) enabled
        algorithms.add(new TabuSearchAlgorithm("TS_Eff_LTM_on_SO_off", maxIterationsRate, tabuTenure, new PDSPRandomConstructive(),new EfficientBasicNeighborhood(),true, 0));

        // TS with Strategic Oscillation (SO) enabled
        algorithms.add(new TabuSearchAlgorithm("TS_Eff_LTM_off_SO_on", maxIterationsRate, tabuTenure, new PDSPRandomConstructive(),new EfficientBasicNeighborhood(),false, strategicOscillationRate));

        // TS with both LTM and SO enabled
        algorithms.add(new TabuSearchAlgorithm("TS_Eff_LTM_on_SO_on", maxIterationsRate, tabuTenure, new PDSPRandomConstructive(),new EfficientBasicNeighborhood(),true, strategicOscillationRate));

        return algorithms;
    }

}
