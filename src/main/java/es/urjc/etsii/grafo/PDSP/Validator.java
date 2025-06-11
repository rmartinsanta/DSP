//package es.urjc.etsii.grafo.PDSP;
//
//import es.urjc.etsii.grafo.PDSP.model.PDSPInstanceImporter;
//import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Scanner;
//
//public class Validator {
//    public static final String PATH = "sotaoutput";
//    public static final String INSTANCEPATH = "instances/testgraphs";
//
//    public static void main(String[] args) throws IOException {
//        Files.list(Path.of(PATH))
//                .filter(f -> !f.getFileName().toString().contains("_Int_"))
//                .forEach(Validator::doCheck);
//    }
//
//    private static void doCheck(Path path) {
//        var instanceName = instanceName(path);
//
//        try(var sc = new Scanner(new FileInputStream(path.toFile()))){
//            while(sc.hasNextInt()){
//                int n = sc.nextInt(); sc.nextLine();
//                String[] parts = sc.nextLine().split(",");
//                if(parts.length != n){
//                    throw new IllegalStateException(String.format("Size mismatch: %s vs %s", n, parts.length));
//                }
//                int[] nodes = new int[n];
//                for (int i = 0; i < nodes.length; i++) {
//                    nodes[i] = Integer.parseInt(parts[i]);
//                }
//                var solution = buildSolution(nodes, instanceName);
//                if(!solution.isCovered()){
//                    throw new IllegalStateException();
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static String instanceName(Path p){
//        var filename = p.getFileName().toString();
//        var parts = filename.split("_");
//        var prefix = parts[0];
//        return prefix + (filename.contains("case")? ".ptxt" : ".graph");
//    }
//
//    private static PDSPSolution buildSolution(int[] nodes, String instanceName){
//        var importer = new PDSPInstanceImporter();
//        var instance = importer.importInstance(new File(INSTANCEPATH, instanceName));
//        var solution = new PDSPSolution(instance);
//        for(int n: nodes){
//            solution.add(n);
//        }
//        return solution;
//    }
//
//
//}
