package config.taxi;

import burlap.debugtools.RandomFactory;
import burlap.visualizer.Visualizer;
import config.DomainConfig;
import config.ExperimentConfig;
import config.output.OutputConfig;
import config.planning.PlanningConfig;
import config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import taxi.TaxiVisualizer;
import taxi.state.TaxiState;
import taxi.stateGenerator.TaxiStateFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class TaxiConfig extends DomainConfig {
    public double correct_move;
    public double fickle;

    public TaxiState generateState() {
        switch (state) {
            case "classic":
                return TaxiStateFactory.createClassicState();
            //added classic20 for the 20x20 map
            case "classic20":
            	return TaxiStateFactory.createClassic20State(1);
            case "classic-2passengers":
                return TaxiStateFactory.createClassicState(2);
            case "tiny":
                return TaxiStateFactory.createTinyState();
            case "tiny-2passengers":
                return TaxiStateFactory.createTinyState(2);
            case "tiny3-2passengers":
                return TaxiStateFactory.createTiny3State(2);
            case "small":
                return TaxiStateFactory.createSmallState();
            case "small-2passengers":
                return TaxiStateFactory.createSmallState(2);
            case "medium":
                return TaxiStateFactory.createMediumState();
            case "medium-2passengers":
                return TaxiStateFactory.createMediumState(2);
            case "3depots":
                return TaxiStateFactory.createThreeDepots();
            case "3depots-2passengers":
                return TaxiStateFactory.createThreeDepots(2);
            case "mehta-zigzag-1":
                return TaxiStateFactory.createMehtaZigZag1State(1);
            case "mehta-zigzag-2":
                return TaxiStateFactory.createMehtaZigZag2State(2);
            default:
                throw new RuntimeException("ERROR: invalid state passed to generateState in TaxiConfig: " + state);
        }
    }

    @Override
    public Visualizer getVisualizer(ExperimentConfig config) {
        return TaxiVisualizer.getVisualizer(config.output.visualizer.width, config.output.visualizer.height);
    }
}
