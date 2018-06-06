package config;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import config.output.OutputConfig;
import config.planning.PlanningConfig;
import config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import taxi.TaxiVisualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static utilities.BurlapConstants.DEFAULT_RNG_INDEX;

public class ExperimentConfig {

    public static long UNSET_LONG = Long.MIN_VALUE;
    public static int UNSET_INT = Integer.MIN_VALUE;
    public static double UNSET_DOUBLE = Double.NEGATIVE_INFINITY;

    public long seed = UNSET_LONG;
    public List<String> agents;
    public int episodes = UNSET_INT;
    public int max_steps = UNSET_INT;
    public int trials = UNSET_INT;
    public double gamma = UNSET_DOUBLE;

    public DomainConfig domain;
    public PlanningConfig planning;
    public RmaxConfig rmax;
    public OutputConfig output;

    public boolean validate() {
        if (seed <= UNSET_LONG) { return false; }
        if (agents == null || agents.size() < 1) { return false; }
        if (episodes == UNSET_INT) { return false; }
        if (max_steps == UNSET_INT) { return false; }
        if (trials == UNSET_INT) { return false; }
        if (gamma == UNSET_DOUBLE) { return false; }
        if (domain == null || !domain.validate()) { return false; }
        if (planning == null || !planning.validate()) { return false; }
        if (rmax == null || !rmax.validate()) { return false; }
        if (output == null || !output.validate()) { return false; }
        return true;
    }


    public static ExperimentConfig load(String conffile) throws FileNotFoundException {

        Constructor constructor = new Constructor(ExperimentConfig.class);

//        TypeDescription typeEntityContextGenerator = new TypeDescription(DomainConfig.class);
//        constructor.addTypeDescription(typeEntityContextGenerator);

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(false);

        Yaml yaml = new Yaml(constructor, representer);
        InputStream input = new FileInputStream(new File(conffile));
        ExperimentConfig config = (ExperimentConfig) yaml.load(input);

        boolean validated = config.validate();
        if (!validated) {
            System.err.println("Error: config file failed to validate -- missing parameters?");
            System.exit(-9921);
        }

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
