package config;

import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import taxi.TaxiVisualizer;

public abstract class DomainConfig {
    public String state;
    public abstract State generateState();
    public abstract Visualizer getVisualizer(ExperimentConfig config);

    public boolean validate() {
        if (state == null || state.equals("")) {
            return false;
        }
        return true;
    }
}
