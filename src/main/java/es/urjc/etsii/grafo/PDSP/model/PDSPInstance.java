package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.*;


/**
 * Represents a PDSP instance, analyzing and extracting several metrics that may be useful both during solving
 * and then during the experiment results analysis
 */
public class PDSPInstance extends Instance {

    /**
     * Graph representation mapping each node to its adjacent nodes
     */
    private final Map<Integer, BitSet> graph;
    /**
     * Set of leafs in the graph. A leaf is a node with only one adjacent node.
     */
    private final BitSet leafs;

    /**
     * Set of support nodes in the graph.
     * A node is support when it is the only neighbor of a leaf.
     * All the support nodes should be part of a solution.
     */
    private final BitSet supportNodes;

    /**
     * Because leafs can never be part of the solution, and critical nodes must always be part of the solution,
     * there is a limited set of nodes that can be added and removed from it.
     * Therefore, the set of nodes that can be added at any point of time becomes configurableNodes - alreadyAddedNodes,
     * and the set of nodes that can be removed is configurableNodes INTERSECT alreadyAddedNodes
     */
    private final BitSet configurableNodes;

    /**
     * Reference solution that contains all critical nodes, used as a base solution
     */
    private final PDSPSolution referenceSolution;


    public PDSPInstance(String name, Map<Integer, Set<Integer>> graph){
        super(name);
        this.graph = new HashMap<>();
        for(var e: graph.entrySet()){
            this.graph.put(e.getKey(), new BitSet(graph.size(), e.getValue()));
        }

        this.leafs = new BitSet(graph.size());
        this.supportNodes = new BitSet(graph.size());

        if(this.graph.size() <= 2){ // Must have at least three elements in the graph
            throw new IllegalArgumentException(String.format("The number of nodes in instance %s is %s, should have at least 3 elements", name, graph.size()));
        }

        classifyNodes();
        this.configurableNodes = calculateConfigurableNodes();
        this.referenceSolution = buildReferenceSolution();
        this.setProperty("nNodes", this.graph.size());
        this.setProperty("leafNodes", this.leafs.size());
        this.setProperty("supportNodes", this.supportNodes.size());
//        this.setProperty("configurableNodes", this.configurableNodes.size());
//        this.setProperty("unmarkedNodes", this.referenceSolution.getUnobservedNodes().size());
        this.setProperty("density", calculateDensity());
        this.setProperty("unobservedNodes", this.referenceSolution.getUnobservedNodes().size());
    }

    private double calculateDensity() {
        int nNodes = this.graph.size();
        int maxEdges = nNodes * (nNodes - 1) / 2;
        int realEdges = 0;
        for(var l: this.graph.values()){
            realEdges += l.size();
        }

        return realEdges / (double) maxEdges;
    }

    private void classifyNodes() {
        int[] leafsPerNode = new int[this.nNodes()];
        for(var e: this.graph.entrySet()){
            var node = e.getKey();
            var neighbors = e.getValue();
            if(neighbors.size() == 1){
                this.leafs.add(node);
                int neighbor = neighbors.iterator().next(); // Size 1 --> Single neighbor
                leafsPerNode[neighbor]++;                   // neighbor has a leaf
                // It may happen that the support is also a leaf, like in O---O (O is a leaf and a support).
                // Then, only one of the two nodes is added as a support node, which
                // will be the first one to be added to the supportNodes set.
                if (!this.leafs.contains(neighbor)) {
                    this.supportNodes.add(neighbor);         // neighbor is a support node
                }
            }
        }
    }

    @Override
    public int compareTo(Instance other) {
        var instance = (PDSPInstance) other;
        return Comparator.comparing(PDSPInstance::nNodes).compare(this, instance);
    }

    public int nNodes(){
        return this.graph.size();
    }

    public Map<Integer, BitSet> graph() {
        return graph;
    }

    public Set<Integer> nodes(){
        return this.graph.keySet();
    }

    public boolean isLeaf(int nodeId) {
        return this.leafs.get(nodeId);
    }

    public BitSet configurableNodes(){
        return this.configurableNodes;
    }

    public boolean isConfigurable(int nodeId){
        return this.configurableNodes.get(nodeId);
    }

    /**
     * Generate a subset of nodes that can be picked amongst all the available nodes
     * Some nodes will never be part of a solution (leafs),
     * and some nodes must always be part of a solution (support nodes)
     * @return nodes that can be picked amongst all the available nodes
     */
    public BitSet calculateConfigurableNodes(){
        var configurableNodes = new BitSet(this.graph.size());
        configurableNodes.add(0, this.graph.size());
        for(var n: this.leafs){
            configurableNodes.remove(n);
        }
        for(var n: this.supportNodes){
            configurableNodes.remove(n);
        }
        return configurableNodes;
    }

    /**
     * Builds a reference solution that contains all support nodes
     * All solutions must contain support nodes in order to be feasible
     * @return reference solution
     */
    public PDSPSolution buildReferenceSolution(){
        var solution = new PDSPSolution(this, true);
        for(var n: supportNodes){
            solution.addNode(n);
        }
        return solution;
    }

    public PDSPSolution referenceSolution(){
        return this.referenceSolution;
    }


    public BitSet getSupportNodes() {
        return supportNodes;
    }
}
