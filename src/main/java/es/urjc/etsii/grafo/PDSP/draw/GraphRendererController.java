package es.urjc.etsii.grafo.PDSP.draw;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.events.AbstractEventStorage;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphRendererController {

    private final GraphRenderer renderer;
    private final AbstractEventStorage<PDSPSolution, PDSPInstance> eventStorage;


    public GraphRendererController(GraphRenderer renderer, AbstractEventStorage<PDSPSolution, PDSPInstance> eventStorage) {
        this.renderer = renderer;
        this.eventStorage = eventStorage;
    }

    @GetMapping("/api/generategraph/{eventId}")
    public ResponseEntity<byte[]> getSolutionAsPNG(@PathVariable int eventId, @RequestParam(defaultValue = "100") int height) {
        // height is ignored, graphviz decides
        var event =  eventStorage.getEvent(eventId);
        if(event instanceof SolutionGeneratedEvent<?, ?> solutionGeneratedEvent){
            var optionalSolution = solutionGeneratedEvent.getSolution();
            if(optionalSolution.isPresent()){
                var solution = (PDSPSolution) optionalSolution.get();
                var renderedData = this.renderer.toPNG(solution);
                return ResponseEntity.ok(renderedData);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
