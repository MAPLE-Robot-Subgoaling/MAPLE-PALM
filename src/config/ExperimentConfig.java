package config;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import config.output.OutputConfig;
import config.planning.PlanningConfig;
import config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import taxi.TaxiVisualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static utilities.BurlapConstants.DEFAULT_RNG_INDEX;

public class ExperimentConfig {

    public long seed;
    public List<String> agents;
    public int episodes;
    public int max_steps;
    public int trials;
    public double gamma;

    public DomainConfig domain;
    public PlanningConfig planning;
    public RmaxConfig rmax;
    public OutputConfig output;

    public static ExperimentConfig load(String conffile) throws FileNotFoundException {

        Constructor constructor = new Constructor(ExperimentConfig.class);

//        TypeDescription typeEntityContextGenerator = new TypeDescription(DomainConfig.class);
//        constructor.addTypeDescription(typeEntityContextGenerator);

        Yaml yaml = new Yaml(constructor);
        InputStream input = new FileInputStream(new File(conffile));
        ExperimentConfig config = (ExperimentConfig) yaml.load(input);
        long seed = config.seed;
        if (seed == 0) {
            seed = System.nanoTime();
            System.err.println("Warning: using a randomly generated RNG seed: " + seed);
        }
        RandomFactory.seedMapped(DEFAULT_RNG_INDEX, seed);
        System.out.println("Using seed: " + config.seed);
        return config;
    }

    public State generateState() {
        return domain.generateState();
    }

    public Visualizer getVisualizer(ExperimentConfig config) {
        return domain.getVisualizer(config);
    }
}
