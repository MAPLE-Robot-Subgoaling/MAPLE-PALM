package edu.umbc.cs.maple.config.output;

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
