package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.palm.agent.PALMModel;

public class UpdatePALMRmaxModelGenerator extends PALMRmaxModelGenerator {
    public UpdatePALMRmaxModelGenerator(HashableStateFactory hsf, ExperimentConfig config) {
        super(hsf, config);
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        return new UpdateBasedRmaxModel(t.getTask(), this.threshold, this.rmax, this.hashingFactory, this.gamma, this.useMultitimeModel);
    }}
