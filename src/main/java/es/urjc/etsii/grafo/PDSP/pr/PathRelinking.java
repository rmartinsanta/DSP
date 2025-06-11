package es.urjc.etsii.grafo.PDSP.pr;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.*;

public class PathRelinking extends SolutionCombinator<PDSPSolution, PDSPInstance> {

    private static final int MAX_STORE_SOL = 1000;

    protected final Reconstructive<PDSPSolution, PDSPInstance> repair;
    private final Improver<PDSPSolution, PDSPInstance> improver;

    public PathRelinking(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        this.repair = repair;
        this.improver = improver;
    }

    protected void addSolution(TreeSet<PDSPSolution> solutions, PDSPSolution solution){
        var copy = solution.cloneSolution();
        copy = this.repair.reconstruct(copy);
        copy = this.improver.improve(copy);
        if (solutions.size() < MAX_STORE_SOL) {
            solutions.add(copy);
        } else if(copy.getScore() < solutions.last().getScore()){
            solutions.pollLast();
            solutions.add(copy);
        }
    }

    @Override
    public Set<PDSPSolution> newSet(PDSPSolution[] currentSet, Set<PDSPSolution> newSolutions) {
        var newsize = newSolutions.size() * currentSet.length;
        var newset = new HashSet<PDSPSolution>(newsize);
        for (var solution : newSolutions) {
            for (var refSolution : currentSet) {
                if(TimeControl.isTimeUp()){
                    return newset;
                }
                var combinedSolution = this.apply(solution, refSolution);
                newset.addAll(combinedSolution);
            }
        }
        return newset;
    }

    @Override
    protected List<PDSPSolution> apply(PDSPSolution origin, PDSPSolution reference) {
        assert origin.getInstance() == reference.getInstance();

        var generatedSolutions = new TreeSet<>(Comparator.comparing(PDSPSolution::getScore));
        var nodesInA = origin.getRemovableNodes();
        var nodesInB = reference.getRemovableNodes();

        // We have to add the nodes that are in B that are not in A, and remove those in A not in B
        var nodesToAdd = BitSet.difference(nodesInB, nodesInA);
        var nodesToRemove = BitSet.difference(nodesInA, nodesInB);

        // Copy solution that is going to be modified during walk
        var current = origin.cloneSolution();

        var iterateRemove = nodesToRemove.iterator();
        while(nodesToRemove.size() > nodesToAdd.size()){
            var nextRemovedNode = iterateRemove.next();
            iterateRemove.remove();
            current.removeNode(nextRemovedNode);
            addSolution(generatedSolutions, current);
        }

        var iterateAdd = nodesToAdd.iterator();
        while(nodesToAdd.size() > nodesToRemove.size()){
            var nextAddedNode = iterateAdd.next();
            iterateAdd.remove();
            current.addNode(nextAddedNode);
            addSolution(generatedSolutions, current);
        }

        // Same size, start swapping
        while(!nodesToAdd.isEmpty()){
            // Swap first element in each set, until set is empty
            var nextAddedNode = iterateAdd.next();
            iterateAdd.remove();
            var nextRemovedNode = iterateRemove.next();
            iterateRemove.remove();
            current.removeNode(nextRemovedNode);
            current.addNode(nextAddedNode);
            addSolution(generatedSolutions, current);
        }

        // because sets have the same size before starting the last while loop,
        // verify they are emptied at the same time
        assert nodesToRemove.isEmpty();

        return new ArrayList<>(generatedSolutions);
    }
}
