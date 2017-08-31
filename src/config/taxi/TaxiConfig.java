package config.taxi;

import burlap.debugtools.RandomFactory;
import config.output.OutputConfig;
import config.planning.PlanningConfig;
import config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import taxi.state.TaxiState;
import taxi.stateGenerator.TaxiStateFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class TaxiConfig {
    public String state;
    public List<String> agents;
    public int episodes;
    public int max_steps;
    public int trials;
    public double gamma;

    public StochasticTaxiConfig stochastic;
    public PlanningConfig planning;
    public RmaxConfig rmax;
    public OutputConfig output;

    public static TaxiConfig load(String conffile) throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(TaxiConfig.class));
        InputStream input = new FileInputStream(new File(conffile));


        RandomFactory.seedMapped(0, 2320942930L);

        return (TaxiConfig) yaml.load(input);
    }

    public TaxiState generateState() {
        switch (state) {
            case "tiny":
                return TaxiStateFactory.createTinyState();
            case "small":
                return TaxiStateFactory.createSmallState();
            default:
                return TaxiStateFactory.createClassicState();
        }
    }
}
