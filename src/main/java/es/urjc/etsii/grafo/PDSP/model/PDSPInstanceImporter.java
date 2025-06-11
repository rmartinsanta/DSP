package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class PDSPInstanceImporter extends InstanceImporter<PDSPInstance> {

    private static final Function<Integer, Set<Integer>> SET_FACTORY = any -> new HashSet<>();

    @Override
    public PDSPInstance importInstance(BufferedReader reader, String filename) throws IOException {
        // Create and return instance object from file data
        Scanner sc = new Scanner(reader);

        Map<Integer, Set<Integer>> adjacency;
        if (!filename.contains(".") || filename.endsWith(".txt")) {
            adjacency = importAdjacencyMatrix(sc);
        } else if (filename.endsWith(".graph")) {
            adjacency = importIEE(sc);
        } else if (filename.endsWith(".ptxt")){
            adjacency = importPTXT(sc);
        } else {
            throw new IllegalArgumentException("Unknown instance type: " +filename);
        }

        var instance = new PDSPInstance(filename, adjacency);
        return instance;
    }

    private Map<Integer, Set<Integer>> importAdjacencyMatrix(Scanner sc) {
        int n = sc.nextInt();
        boolean[][] matrix = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                boolean isConnected = sc.nextInt() == 1;
                matrix[i][j] = isConnected;
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if(matrix[i][j] != matrix[j][i]){
                    throw new IllegalArgumentException("Invalid instance, not symmetric at [%s, %s]".formatted(i, j));
                }
            }
        }

        // Unused, try parse anyway
        int domination = sc.nextInt();

        // There shouldnt be any data left to parse
        if(sc.hasNextInt()){
            throw new IllegalArgumentException("Invalid instance, extra data after parsing: " + sc.nextInt());
        }

        // Transform adjacency matrix to Map of Sets
        var adjacency = new HashMap<Integer, Set<Integer>>();
        for (int i = 0; i < n; i++) {
            var set = new HashSet<Integer>();
            for (int j = 0; j < n; j++) {
                if(matrix[i][j]){
                    set.add(j);
                }
            }
            adjacency.put(i, set);
        }
        return adjacency;
    }

    private Map<Integer, Set<Integer>> importIEE(Scanner sc) {
        // WARN: IEEE format is 1 indexed, PTXT is 0 indexed
        var graph = new HashMap<Integer, Set<Integer>>();

        int nodes = sc.nextInt(), edges = sc.nextInt();
        for (int i = 0; i < edges; i++) {
            int left = sc.nextInt() - 1, right = sc.nextInt() - 1;
            graph.computeIfAbsent(left, SET_FACTORY);
            graph.computeIfAbsent(right, SET_FACTORY);
            graph.get(left).add(right);
            graph.get(right).add(left);
        }
        if (graph.size() != nodes) {
            throw new IllegalArgumentException(String.format("Invalid instance file, declared %s nodes but only contains %s", nodes, graph.size()));
        }

        verifyStronglyConnected(graph);
        return graph;
    }

    private Map<Integer, Set<Integer>> importPTXT(Scanner sc) {
        // WARN: IEEE format is 1 indexed, PTXT is 0 indexed
        var graph = new HashMap<Integer, Set<Integer>>();

        expect("Number Of Vertices", sc);
        int nodes = sc.nextInt();

        expect("Edges", sc);
        for (int i = 0; i < nodes; i++) {
            expect("Number edges containting node " + i, sc);
            int nNeighbors = sc.nextInt();
            if (nNeighbors < 1) {
                throw new IllegalArgumentException(String.format("Node %s does not have at least one neighborhood", i));
            }
            graph.computeIfAbsent(i, SET_FACTORY);
            expect("list", sc);
            for (int j = 0; j < nNeighbors; j++) {
                int neighbor = sc.nextInt();
                graph.get(i).add(neighbor);
            }
        }

        verifySymmetry(graph);
        verifyStronglyConnected(graph);
        return graph;
    }

    private void verifySymmetry(Map<Integer, Set<Integer>> graph) {
        for (int a : graph.keySet()) {
            for (var b : graph.get(a)) {
                if (!graph.get(b).contains(a)) {
                    throw new IllegalArgumentException(String.format("Invalid instance file: non symmetric data, %s -> %s exist but %s -> %s does NOT", a, b, b, a));
                }
            }
        }
    }

    private void verifyStronglyConnected(Map<Integer, Set<Integer>> graph) {
        int nComponents = countComponents(graph);
        if (nComponents != 1) {
            throw new IllegalArgumentException("Invalid instances, expected one components, graph has " + nComponents);
        }
    }

    private int countComponents(Map<Integer, Set<Integer>> graph) {
        boolean[] visited = new boolean[graph.size()];
        int components = 0;
        for (int i = 0; i < graph.size(); i++) {
            if (!visited[i]) {
                components++;
                bfs(visited, graph, i);
            }
        }
        return components;
    }

    private void bfs(boolean[] visited, Map<Integer, Set<Integer>> graph, int startNode) {
        if (visited[startNode]) {
            throw new IllegalArgumentException("Already visited " + startNode);
        }

        Queue<Integer> queue = new ArrayDeque<>(graph.size());
        visited[startNode] = true;
        queue.add(startNode);
        while (!queue.isEmpty()) {
            int current = queue.remove();
            for (var n : graph.get(current)) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }

    private static void expect(String expectedToken, Scanner sc) {
        var parts = expectedToken.split("\\s+");
        for (var part : parts) {
            String currentToken = sc.next();
            if (!currentToken.equals(part)) {
                throw new IllegalArgumentException(String.format("Expected %s token, got: %s", expectedToken, currentToken));
            }
        }
    }
}
