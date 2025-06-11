//package es.urjc.etsii.grafo.PDSP.experiments;
//
//
//import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
//import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.Map;
//
//public class PDSPReferenceResults extends ReferenceResultProvider {
//
//    private final Map<String, ReferenceResult> sotaResults = new HashMap<>();
//
//    public PDSPReferenceResults() throws IOException {
//        // For each line in our CSV/TSV file with the following structure, fill the hashmap
//        // instanceName,o.fValue,executionTime
//        Files.lines(Path.of("sota.csv")).forEach(l -> {
//            var parts = l.split(",");
//            var referenceResult = new ReferenceResult();
//            referenceResult.setScore(parts[1]);         // Change columns if necessary
//            referenceResult.setTimeInSeconds(parts[2]); // Use .setTimeToBestInSeconds if it is a TTB instead of total time
//            sotaResults.put(parts[0], referenceResult); // Instance name is usually the first column
//        });
//    }
//
//    @Override
//    public ReferenceResult getValueFor(String instanceName) {
//        // Return reference result if exists, empty one if not.
//        // It is perfectly valid to have instances for which the reference values are unknown.
//        var optResult = this.sotaResults.get(instanceName);
//        if(optResult == null){
//            optResult = this.sotaResults.getOrDefault(instanceName + ".txt", new ReferenceResult());
//        }
//        return optResult;
//    }
//
//    @Override
//    public String getProviderName() {
//        // There can be multiple providers (i.e, multiple classes extending ReferenceResultProvider)
//        // as long as they have different provider names
//        return "DSP SOTA";
//    }
//}
