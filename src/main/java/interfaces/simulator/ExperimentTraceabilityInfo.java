package interfaces.simulator;

import java.util.Map;
import java.util.Properties;

public interface ExperimentTraceabilityInfo {

    Properties auxiliaryInformation();

    Map<String, Number> statistics();
}
