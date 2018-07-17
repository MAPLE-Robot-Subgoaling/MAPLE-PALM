package edu.umbc.cs.maple.config;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.config.output.OutputConfig;
import edu.umbc.cs.maple.config.planning.PlanningConfig;
import edu.umbc.cs.maple.config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static edu.umbc.cs.maple.utilities.BurlapConstants.DEFAULT_RNG_INDEX;

public class ExperimentConfig {

    public static long UNSET_LONG = Long.MIN_VALUE;
    public static int UNSET_INT = Integer.MIN_VALUE;
    public static double UNSET_DOUBLE = Double.NEGATIVE_INFINITY;
    public static Boolean UNSET_BOOLEAN = null;

    public long seed = UNSET_LONG;
    public Map<String,List<String>> agents;
    public int episodes = UNSET_INT;
    public int max_steps = UNSET_INT;
    public int trials = UNSET_INT;
    public Boolean identifier_independent = UNSET_BOOLEAN;

    public DomainConfig domain;
    public PlanningConfig planning;
    public RmaxConfig rmax;
    public OutputConfig output;
    public double gamma;
    public DomainGoal goal;


    public static ExperimentConfig load(String configFile) throws FileNotFoundException {

        Constructor constructor = new Constructor(ExperimentConfig.class);

//        TypeDescription typeEntityContextGenerator = new TypeDescription(DomainConfig.class);
//        constructor.addTypeDescription(typeEntityContextGenerator);

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(false);

        Yaml yaml = new Yaml(constructor, representer);
        InputStream input = ClassLoader.getSystemResourceAsStream(configFile);
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

    public static ExperimentConfig loadConfig(String configFile) {
        ExperimentConfig config = new ExperimentConfig();
        try {
            System.out.println("Using configuration: " + configFile);
            config = ExperimentConfig.load(configFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Could not find configuration file");
            System.exit(404);
        }
        return config;
    }

}
