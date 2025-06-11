package es.urjc.etsii.grafo.PDSP.constructives.grasp;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.ArrayList;
import java.util.List;

public class SmartPDSPListManager extends GRASPListManager<PDSPGRASPMove, PDSPSolution, PDSPInstance> {

    protected final boolean firstRandom;

    public SmartPDSPListManager(boolean firstRandom) {
        this.firstRandom = firstRandom;
    }

    @Override
    public void beforeGRASP(PDSPSolution solution) {
        super.beforeGRASP(solution);

        if(this.firstRandom){
            var candidates = solution.getAddableNodes();
            var firstNode = CollectionUtil.pickRandom(candidates);
            solution.addNode(firstNode);
        }
    }

    /**
     * Generate initial candidate list. The list will be sorted if necessary by the constructive method.
     * @param solution Current solution
     * @return a candidate list
     */
    @Override
    public List<PDSPGRASPMove> buildInitialCandidateList(PDSPSolution solution) {
        if(solution.isCovered()) {
            return List.of();
        }

        var list = new ArrayList<PDSPGRASPMove>();
        var candidateNodes = calculateCandidateNodes(solution);
        for(var nodeId: candidateNodes){
            list.add(new PDSPGRASPMove(solution, nodeId));
        }

        return list;
    }

    public BitSet calculateCandidateNodes(PDSPSolution solution){
        return BitSet.difference(solution.getInstance().configurableNodes(), solution.getChosen());
    }

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     * @param solution Current solution, move has been already applied
     * @param move     Chosen move
     * @param index index of the chosen move in the candidate list
     * @param candidateList original candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    @Override
    public List<PDSPGRASPMove> updateCandidateList(PDSPSolution solution, PDSPGRASPMove move, List<PDSPGRASPMove> candidateList, int index) {
        // this can be optimized if necessary later as only the neighbors of the chosen node are affected
        return buildInitialCandidateList(solution);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "firstRandom=" + firstRandom +
                '}';
    }
}
