package es.urjc.etsii.grafo.PDSP.io;

import es.urjc.etsii.grafo.annotation.SerializerSource;
import es.urjc.etsii.grafo.io.serializers.AbstractSolutionSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SerializerSource
@ConfigurationProperties("serializers.graph")
public class PDSPSolutionConfig extends AbstractSolutionSerializerConfig {
}
