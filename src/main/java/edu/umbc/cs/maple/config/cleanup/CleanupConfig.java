package edu.umbc.cs.maple.config.cleanup;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.cleanup.CleanupVisualizer;
import edu.umbc.cs.maple.cleanup.state.CleanupRandomStateGenerator;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.config.DomainConfig;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.config.output.OutputConfig;
import edu.umbc.cs.maple.config.planning.PlanningConfig;
import edu.umbc.cs.maple.config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_DOUBLE;
import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_INT;
import static edu.umbc.cs.maple.utilities.BurlapConstants.DEFAULT_RNG_INDEX;

public class CleanupConfig extends DomainConfig {

    public double rewardGoal = UNSET_DOUBLE;
    public double rewardBase = UNSET_DOUBLE;
    public double rewardNoop = UNSET_DOUBLE;
    public double rewardPull = UNSET_DOUBLE;
    public int minX = UNSET_INT;
    public int minY = UNSET_INT;
    public int maxX = UNSET_INT;
    public int maxY = UNSET_INT;
    public int num_blocks = UNSET_INT;

    @Override
    public boolean validate() {
        boolean valid = super.validate();
        if (!valid) { return false; }
        if (rewardGoal == UNSET_DOUBLE) { return false; }
        if (rewardBase == UNSET_DOUBLE) { return false; }
        if (rewardNoop == UNSET_DOUBLE) { return false; }
        if (rewardPull == UNSET_DOUBLE) { return false; }
        if (minX == UNSET_DOUBLE) { return false; }
        if (minY == UNSET_DOUBLE) { return false; }
        if (maxX == UNSET_DOUBLE) { return false; }
        if (maxY == UNSET_DOUBLE) { return false; }
        if (num_blocks == UNSET_DOUBLE) { return false; }
        return true;
    }

    @Override
    public State generateState() {
        return (CleanupState) new CleanupRandomStateGenerator(minX, minY, maxX, maxY).getStateFor(state, num_blocks);
    }

    @Override
    public Visualizer getVisualizer(ExperimentConfig config) {
        return CleanupVisualizer.getVisualizer(config.output.visualizer.width, config.output.visualizer.height);
    }

    @Override
    public DomainGenerator getDomainGenerator() {
        return null;
    }

}
