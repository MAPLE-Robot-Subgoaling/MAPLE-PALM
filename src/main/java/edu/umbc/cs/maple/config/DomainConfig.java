package edu.umbc.cs.maple.config;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.taxi.TaxiVisualizer;

public abstract class DomainConfig {
    public String state;
    public abstract State generateState();
    public abstract Visualizer getVisualizer(ExperimentConfig config);
    public abstract DomainGenerator getDomainGenerator();
    public boolean validate() {
        if (state == null || state.equals("")) {
            return false;
        }
        return true;
    }
}
