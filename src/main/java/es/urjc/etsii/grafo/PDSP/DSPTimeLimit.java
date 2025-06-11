package es.urjc.etsii.grafo.PDSP;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DSPTimeLimit extends TimeLimitCalculator<PDSPSolution, PDSPInstance> {

    private static final Logger log = LoggerFactory.getLogger(DSPTimeLimit.class);
    private static final String TIMELIMIT_FILE = "timelimits.csv";
    Map<String, Double> timePerInstanceInSecs;

    public DSPTimeLimit() {
        var f = new File(TIMELIMIT_FILE);
        if(!f.isFile()){
            throw new IllegalArgumentException("Timelimit file %s not found".formatted(f.getAbsolutePath()));
        }
        try(Stream<String> lines = Files.lines(f.toPath())) {
            this.timePerInstanceInSecs = lines
                    .map(l -> l.split(","))
                    .collect(Collectors.toMap(parts -> parts[0], parts -> Double.parseDouble(parts[1])));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long timeLimitInMillis(PDSPInstance instance, Algorithm<PDSPSolution, PDSPInstance> algorithm) {
        var limit = this.timePerInstanceInSecs.get(instance.getId());
        if (limit == null || !Double.isFinite(limit)) {
            log.warn("No time limit found for instance {}, setting to 1 second", instance.getId());
            return 1_000;
        }
        return (long) (limit * 1_000);
    }
}
