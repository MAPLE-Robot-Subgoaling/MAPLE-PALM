package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;

public class ExpectedRmaxModelGenerator implements PALMModelGenerator {

    private int threshold;
    private double rmax;
    private double gamma;
    private HashableStateFactory hashingFactory;

    public ExpectedRmaxModelGenerator(HashableStateFactory hsf, double gamma, int threshold, double rmax) {
        this.hashingFactory = hsf;
        this.gamma = gamma;
        this.threshold = threshold;
        this.rmax = rmax;
    }

    public ExpectedRmaxModelGenerator(HashableStateFactory hsf, ExperimentConfig config) {
        this(hsf, config.gamma, config.rmax.threshold, config.rmax.vmax);
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        return new ExpectedRmaxModel(t, this.threshold, this.rmax, this.hashingFactory, this.gamma);
    }
}
