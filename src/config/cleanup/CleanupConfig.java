package config.cleanup;

import burlap.debugtools.RandomFactory;
import cleanup.state.CleanupRandomStateGenerator;
import cleanup.state.CleanupState;
import config.output.OutputConfig;
import config.planning.PlanningConfig;
import config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class CleanupConfig {

    public long seed;
    public int minX;
    public int minY;
    public int maxX;
    public int maxY;
    public double rewardGoal;
    public double rewardBase;
    public double rewardNoop;
    public double rewardPull;
    public int numBlocks;
    public String state;
    public List<String> agents;
    public int episodes;
    public int max_steps;
    public int trials;
    public double gamma;

    public PlanningConfig planning;
    public RmaxConfig rmax;
    public OutputConfig output;

    public static CleanupConfig load(String conffile) throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(CleanupConfig.class));
        InputStream input = new FileInputStream(new File(conffile));
        CleanupConfig config = (CleanupConfig) yaml.load(input);
        long seed = config.seed;
        if (seed == 0) {
            seed = System.nanoTime();
            System.err.println("Warning: using a randomly generated RNG seed: " + seed);
        }
        RandomFactory.seedMapped(0, seed);
        System.out.println("Using seed: " + config.seed);
        return config;
    }

    public CleanupState generateState() {
        return (CleanupState) new CleanupRandomStateGenerator(minX, minY, maxX, maxY).getStateFor(state, numBlocks);
    }
}
