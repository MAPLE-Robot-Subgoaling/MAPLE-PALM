package edu.umbc.cs.maple.config.cleanup;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateGenerator;
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

    public State generateState() {
        Cleanup cleanup = (Cleanup) domainGenerator;
        int minX = cleanup.getMinX();
        int minY = cleanup.getMinY();
        int maxX = cleanup.getMaxX();
        int maxY = cleanup.getMaxY();
        //prevents generating states already solved/terminal
        CleanupRandomStateGenerator gen = new CleanupRandomStateGenerator(minX, minY, maxX, maxY);
        State st;
        do{
            st = gen.getStateFor(state);
        }while(cleanup.getTf().isTerminal(st));

        return st;
    }

    @Override
    public Visualizer getVisualizer(ExperimentConfig config) {
        return CleanupVisualizer.getVisualizer(config.output.visualizer.width, config.output.visualizer.height);
    }


}
