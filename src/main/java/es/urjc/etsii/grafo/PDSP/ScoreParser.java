package es.urjc.etsii.grafo.PDSP;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.etsii.grafo.metrics.BestObjective;
import es.urjc.etsii.grafo.metrics.TimeValue;
import es.urjc.etsii.grafo.util.TimeUtil;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScoreParser {

    private static final ObjectMapper jsonParser = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN);

    private static final Map<String, List<TreeSet<TimeValue>>> data = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java -jar file.jar path2File");
            System.exit(-1);
        }

        var f = new File(args[0]);
        if(!f.exists()) {
            System.out.println("File not found: " + f);
            System.exit(-1);
        }
        var workUnit = parseWorkUnit(f);
        var bestObjectiveEvolution = workUnit.data.metrics().metrics().get(BestObjective.class.getSimpleName());
        System.out.printf("Time (s)\tObj. F.\n");
        for(var timeValue : bestObjectiveEvolution.values){
            var time = TimeUtil.nanosToSecs(timeValue.instant());
            System.out.printf("%s\t%s\n", time, timeValue.value());
        }
    }

    private static RPDFile parseWorkUnit(File f) {
        try {
            var data = jsonParser.readValue(f, RPDData.class);
            return new RPDFile(data, f.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    record AlgName(String shortName, String name) {
        @Override
        public String name() {
            if(shortName != null && !shortName.isBlank()){
                return shortName;
            }
            if(name != null && !name.isBlank()){
                return name;
            }
            throw new IllegalArgumentException("Invalid algorithm name: " + this);
        }
    }

    record RPDData(AlgName algorithm, String iteration, SolutionScore solution, Metrics metrics){
        public double score(){
            return solution.score();
        }
        public String algorithmName(){
            return algorithm.name();
        }
    }

    record RPDFile(RPDData data, String filename){}
    record SolutionScore(double score){}
    record Metrics(long referenceNanoTime, Map<String, Metric> metrics){}
    record Metric(String name, TreeSet<TimeValue> values){}
}