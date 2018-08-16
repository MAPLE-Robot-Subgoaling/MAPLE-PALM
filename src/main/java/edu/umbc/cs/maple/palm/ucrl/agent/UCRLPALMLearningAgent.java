package edu.umbc.cs.maple.palm.ucrl.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.PALMLearningAgent;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;

public class UCRLPALMLearningAgent extends PALMLearningAgent {

    public UCRLPALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, ExperimentConfig config) {
        super(root, modelGenerator, hsf, config);
    }

    @Override
    protected Action nextAction(GroundedTask task, State s) {
        PALMModel model = getModel(task);
        UCRLModel ucrlModel = (UCRLModel) model;
        return ucrlModel.nextAction(s);
    }
}