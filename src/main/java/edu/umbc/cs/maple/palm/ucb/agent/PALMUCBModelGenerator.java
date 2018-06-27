package edu.umbc.cs.maple.palm.ucb.agent;

import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;

public class PALMUCBModelGenerator implements PALMModelGenerator {

    private double gamma;
    private HashableStateFactory hashingFactory;

    public PALMUCBModelGenerator(double gamma, HashableStateFactory hs){
        this.gamma = gamma;
        this.hashingFactory = hs;
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        return new UCBModel(t, gamma);
    }
}
