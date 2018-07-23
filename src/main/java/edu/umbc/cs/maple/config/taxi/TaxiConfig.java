package edu.umbc.cs.maple.config.taxi;

import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.config.DomainConfig;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.taxi.TaxiVisualizer;
import edu.umbc.cs.maple.taxi.state.TaxiState;
import edu.umbc.cs.maple.taxi.stategenerator.TaxiStateFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaxiConfig extends DomainConfig {

    public double correct_move;
    public double fickle;

    public TaxiState generateState() {
        return TaxiStateFactory.generateState(this.state);
    }

    @Override
    public Visualizer getVisualizer(ExperimentConfig config) {
        return TaxiVisualizer.getVisualizer(config.output.visualizer.width, config.output.visualizer.height);
    }
}
