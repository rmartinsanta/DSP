package es.urjc.etsii.grafo.PDSP.draw;

import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

@Service
public class GraphRenderer {

    public byte[] toPNG(PDSPSolution solution){
        Graphviz graphviz = buildGraphviz(solution);
        BufferedImage image = graphviz.render(Format.PNG).toImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    /**
     * Render the graph to a DOT string that can be seen as image using Graphviz
     * <a href="https://dreampuf.github.io/GraphvizOnline/">Graphviz Online renderer</a>
     *
     * @param solution solution to render
     * @return DOT string
     */
    public String toDOT(PDSPSolution solution){
        Graphviz graphviz = buildGraphviz(solution);
        return graphviz.render(Format.DOT).toString();
    }

    public Graphviz buildGraphviz(PDSPSolution solution){
        MutableGraph graph = mutGraph();
        graph.setDirected(false);
        graph.graphAttrs().add("layout", "fdp");
        graph.nodeAttrs().add("style", "filled");
        graph.nodeAttrs().add("fillcolor", "lightgrey");
        graph.nodeAttrs().add("fixedsize", "true");
        graph.nodeAttrs().add("shape", "circle");
        graph.nodeAttrs().add("fontname", "Helvetica,Arial,sans-serif");
        var instance = solution.getInstance();
        var chosen = solution.getChosen();
        int nNodes = instance.nNodes();
        MutableNode[] gNodes = new MutableNode[nNodes];
        var neighborsToChosen = calculateLvl1(solution);
        for (int i = 0; i < nNodes; i++) {
            var gNode = mutNode(String.valueOf(i));
            if(instance.isLeaf(i)){
                gNode.add(Color.BROWN1);
                gNode.add(Color.BROWN1.font());
            }
            if(chosen.contains(i)){
                gNode.add(Color.GREEN.fill());
            } else {
                if (neighborsToChosen.contains(i)) {
                    gNode.add(Color.BLUE);
                    gNode.add(Color.WHITE.fill());
                }
            }
//            if(instance.isCritical(i)){
//                gNode.add(Color.DARKOLIVEGREEN2.fill());
//            }
            gNodes[i] = gNode;
            graph.add(gNode);
        }
        for (int i = 0; i < nNodes; i++) {
            for(var n: solution.getNeighbors(i)){
                if(n > i){
                    gNodes[i].addLink(gNodes[n]);
                }
            }
        }

        Graphviz.useEngine(new GraphvizJdkEngine());
        return Graphviz.fromGraph(graph);
    }

    private Set<Integer> calculateLvl1(PDSPSolution solution){
        var set = new HashSet<Integer>();
        for(var chosen: solution.getChosen()){
            set.addAll(solution.getNeighbors(chosen));
        }
        return set;
    }
}
