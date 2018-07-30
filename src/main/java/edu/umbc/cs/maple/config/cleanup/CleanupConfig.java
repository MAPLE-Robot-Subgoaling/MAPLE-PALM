package edu.umbc.cs.maple.config.cleanup;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.CleanupGoal;
import edu.umbc.cs.maple.cleanup.CleanupVisualizer;
import edu.umbc.cs.maple.cleanup.state.CleanupRandomStateGenerator;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.config.DomainConfig;
import edu.umbc.cs.maple.config.DomainGoal;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.utilities.OOSADomainGenerator;

import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_DOUBLE;
import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_INT;

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
    public OOSADomainGenerator initializeDomainGenerator() {
        Cleanup cleanup = new Cleanup(minX, minY, maxX, maxY);
        return cleanup;
    }


}
