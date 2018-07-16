package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;

public class PALMRmaxModelGenerator implements PALMModelGenerator {

    protected int threshold;
    protected double rmax;
    protected double gamma;
    protected boolean useMultitimeModel;
    protected HashableStateFactory hashingFactory;

    public PALMRmaxModelGenerator(HashableStateFactory hsf, double gamma, int threshold, double rmax, boolean useMultitimeModel) {
        this.hashingFactory = hsf;
        this.gamma = gamma;
        this.threshold = threshold;
        this.rmax = rmax;
        this.useMultitimeModel = useMultitimeModel;
    }

    public PALMRmaxModelGenerator(HashableStateFactory hsf, ExperimentConfig config) {
        this(hsf, config.gamma, config.rmax.threshold, config.rmax.vmax, config.rmax.use_multitime_model);
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        return new HierarchicalRmaxModel(t.getTask(), this.threshold, this.rmax, this.hashingFactory, this.gamma, this.useMultitimeModel);
    }
}
