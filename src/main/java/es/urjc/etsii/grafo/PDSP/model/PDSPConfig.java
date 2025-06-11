package es.urjc.etsii.grafo.PDSP.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tabu-search")
public class PDSPConfig {
    // Define any configuration property to automatically set from any source,
    // including the command line, the application.yml or the environment
    // See: https://mork-optimization.readthedocs.io/en/latest/features/config/ for more details

    /**
     * Value from: custom.my-property, for example from the application.yml file,
     * could be overridden as a command line parameter: --custom.my-property=value
     */
    double maxIterationsRate;
    double tabuTenure;
    boolean useStrategicOscillation;
    boolean useLongTermMemory;
    double strategicOscillationRate;

    public double getMaxIterationsRate() {
        return maxIterationsRate;
    }

    public void setMaxIterationsRate(double maxIterationsRate) {
        this.maxIterationsRate = maxIterationsRate;
    }

    public double getTabuTenure() { return tabuTenure; }

    public void setTabuTenure(double tabuTenure) {
        this.tabuTenure = tabuTenure;
    }

    public boolean isUseStrategicOscillation() {
        return useStrategicOscillation;
    }

    public void setUseStrategicOscillation(boolean useStrategicOscillation) {
        this.useStrategicOscillation = useStrategicOscillation;
    }

    public boolean isUseLongTermMemory() {
        return useLongTermMemory;
    }

    public void setUseLongTermMemory(boolean useLongTermMemory) {
        this.useLongTermMemory = useLongTermMemory;
    }

    public double getStrategicOscillationRate() {
        return strategicOscillationRate;
    }

    public void setStrategicOscillationRate(double strategicOscillationRate) {
        this.strategicOscillationRate = strategicOscillationRate;
    }
}
