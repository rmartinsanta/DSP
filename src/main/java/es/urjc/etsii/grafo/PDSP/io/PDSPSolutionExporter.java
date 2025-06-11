package es.urjc.etsii.grafo.PDSP.io;

import es.urjc.etsii.grafo.PDSP.draw.GraphRenderer;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PDSPSolutionExporter extends SolutionSerializer<PDSPSolution, PDSPInstance> {

    private final GraphRenderer renderer;

    /**
     * Create a new solution serializer with the given config
     *
     * @param config
     * @param renderer
     */
    public PDSPSolutionExporter(PDSPSolutionConfig config, GraphRenderer renderer) {
        super(config);
        this.renderer = renderer;
    }

    @Override
    public void export(String folder, String suggestedFilename, WorkUnitResult<PDSPSolution, PDSPInstance> workunit) {
        // Generate only dot files, PNGs can be built later using ./png.sh
        var dot = this.renderer.toDOT(workunit.solution());
        try {
            Files.writeString(Path.of(folder, suggestedFilename + ".dot"), dot);
            Files.writeString(Path.of(folder, suggestedFilename + ".txt"), workunit.solution().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void export(BufferedWriter writer, WorkUnitResult<PDSPSolution, PDSPInstance> result) throws IOException {
        throw new UnsupportedOperationException("Not used");
    }
}
