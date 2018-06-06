package config.output;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;

public class OutputConfig {
    public CSVConfig csv;
    public ChartConfig chart;
    public VisualizerConfig visualizer;


    public boolean validate() {
        if (csv == null) { return false; }
        if (chart == null) { return false; }
        if (visualizer == null) { return false; }
        return true;
    }
}
