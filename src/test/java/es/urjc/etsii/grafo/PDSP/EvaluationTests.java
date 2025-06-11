//package es.urjc.etsii.grafo.PDSP;
//
//import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
//import es.urjc.etsii.grafo.PDSP.model.PDSPInstanceImporter;
//import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.io.File;
//import java.util.Map;
//import java.util.Set;
//
//class EvaluationTests {
//
//    private static int[] NONE = new int[]{};
//
//    PDSPInstance initInstanceLine() {
//        return new PDSPInstance("Test_line", Map.of(
//                0, Set.of(1),
//                1, Set.of(0, 2),
//                2, Set.of(1, 3),
//                3, Set.of(2, 4),
//                4, Set.of(3)
//        ));
//    }
//
//
//    PDSPInstance initInstanceTriangleLine() {
//        return new PDSPInstance("Test_star", Map.of(
//                0, Set.of(1, 2),
//                1, Set.of(0, 2, 3),
//                2, Set.of(0, 1, 3, 4),
//                3, Set.of(1, 2),
//                4, Set.of(2)
//        ));
//    }
//
//    PDSPInstance initInstanceStar() {
//        // 0 center with radius length 2
//        return new PDSPInstance("Test_star", Map.of(
//                0, Set.of(1, 3, 5),
//                1, Set.of(0, 2),
//                2, Set.of(1),
//                3, Set.of(0, 4),
//                4, Set.of(3),
//                5, Set.of(0, 6),
//                6, Set.of(5)
//        ));
//    }
//
//    @Test
//    void testEvaluateLine() {
//        // Marking a line from either side propagates to the other side completely.
//        // Marking a node in the middle too.
//        var instance = initInstanceLine();
//        int nNodes = instance.nNodes();
//        for (int i = 0; i < instance.nNodes(); i++) {
//            checkCovered(new int[]{i}, instance); // a instance resembling a line must be covered after adding a single node
//            checkWouldAdd(NONE, i, nNodes, instance); // and therefore the method that calculates how many nodes another node observes must return instance size
//        }
//    }
//
//    @Test
//    void testEvaluateStar() {
//        // Marking a line from either side propagates to the other side completely.
//        // Marking a node in the middle too.
//        var instance = initInstanceStar();
//        int nNodes = instance.nNodes();
//        checkCovered(new int[]{0}, instance);
//        checkWouldAdd(NONE, 0, nNodes, instance);
//        for (int i = 1; i < instance.nNodes(); i++) {
//            checkUncovered(new int[]{i}, instance, instance.nNodes() - 3);
//            checkWouldAdd(NONE, i, 3, instance); // Adding any node not in center would only observe the node in the current radius of the star
//        }
//    }
//
//    @Test
//    void testEvaluateTriangles() {
//        // Marking a line from either side propagates to the other side completely.
//        // Marking a node in the middle too.
//        var instance = initInstanceTriangleLine();
//        checkCovered(new int[]{0}, instance);
//    }
//
//    @Test
//    void evaluate4x4Grid() {
//        var importer = new PDSPInstanceImporter();
//        var instance = importer.importInstance(new File("instances/nuevas/Grid/Grid 4x4.txt"));
//        // Adding a node in center marks itself and only the inmediate neighbours -> 5 nodes
//        var tempSolution = new PDSPSolution(instance);
//        int expected = 5;
//        int calculated = tempSolution.testAdd(5);
//        Assertions.assertEquals(expected, calculated);
//    }
//
//    private void checkCovered(int[] nodesToAdd, PDSPInstance instance) {
//        PDSPSolution solution = initSolution(nodesToAdd, instance);
//
//        Assertions.assertTrue(solution.isCovered());
//        Assertions.assertTrue(solution.getUnobservedNodes().isEmpty());
//        Assertions.assertEquals(nodesToAdd.length, solution.getChosen().size());
//    }
//
//    private void checkWouldAdd(int[] startingNodes, int nodeToAdd, int expectedResult, PDSPInstance instance) {
//        PDSPSolution solution = initSolution(startingNodes, instance);
//        int notObserved = solution.getUnobservedNodes().size();
//        var copy = solution.cloneSolution();
//        int calculated = copy.testAdd(nodeToAdd);
//        Assertions.assertEquals(solution, copy); // Assert no side effects
//        solution.add(nodeToAdd);
//        int delta = notObserved - solution.getUnobservedNodes().size();
//
//        Assertions.assertEquals(calculated, delta); // assert node that would be observed if added matches real result after adding the node
//        Assertions.assertEquals(calculated, expectedResult);
//    }
//
//    private void checkUncovered(int[] nodesToAdd, PDSPInstance instance, int uncovered) {
//        PDSPSolution solution = initSolution(nodesToAdd, instance);
//
//        Assertions.assertFalse(solution.isCovered());
//        Assertions.assertEquals(uncovered, solution.getUnobservedNodes().size());
//        Assertions.assertEquals(nodesToAdd.length, solution.getChosen().size());
//    }
//
//    private PDSPSolution initSolution(int[] nodesToAdd, PDSPInstance instance) {
//        var solution = new PDSPSolution(instance);
//        for (int i : nodesToAdd) {
//            solution.add(i);
//        }
//        return solution;
//    }
//
//    @Test
//    void testGrid11() {
//        var importer = new PDSPInstanceImporter();
//        var instance = importer.importInstance(new File("instances/nuevas/Grid/Grid 11x11.txt"));
//        var solution = new PDSPSolution(instance);
//
//        //solution.add(90);
//        var copy = solution.cloneSolution();
//        Assertions.assertEquals(5, copy.testAdd(23));
//        Assertions.assertEquals(solution, copy);
//        solution.add(23);
//        copy.add(23);
//        Assertions.assertEquals(solution, copy);
//
////        Assertions.assertEquals(31, copy.testAdd(46)); // TODO fix test
//        Assertions.assertEquals(solution, copy);
//        solution.add(46);
//        copy.add(46);
//        Assertions.assertEquals(solution, copy);
//
////        Assertions.assertEquals(-1, copy.testAdd(49));
//        Assertions.assertEquals(solution, copy);
//        solution.add(49);
//        copy.add(49);
//        Assertions.assertEquals(solution, copy);
//
//        Assertions.assertTrue(solution.isCovered());
//    }
//
//    @Test
//    void IEE14() {
//        // Failed test during refactor
//        var importer = new PDSPInstanceImporter();
//        var instance = importer.importInstance(new File("instances/testgraphs/IEEE-14.graph"));
//        var solution = new PDSPSolution(instance);
//        solution.add(1);
//        solution.add(13);
//        Assertions.assertTrue(solution.isCovered());
//    }
//}
