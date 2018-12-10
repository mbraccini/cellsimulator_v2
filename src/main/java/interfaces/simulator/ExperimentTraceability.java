package interfaces.simulator;

import java.util.Map;
import java.util.Properties;

public class ExperimentTraceability implements ExperimentTraceabilityInfo {

    private final Properties aux;
    private final Map<String, Number> stats;

    public ExperimentTraceability(Properties aux, Map<String, Number> stats){
        this.aux = aux;
        this.stats = stats;
    }

    @Override
    public Properties auxiliaryInformation() {
        return aux;
    }

    @Override
    public Map<String, Number> statistics() {
        return stats;
    }
}
