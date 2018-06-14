package palm.rmax.agent;

import burlap.statehashing.HashableStateFactory;
import config.ExperimentConfig;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import palm.agent.PALMModel;
import palm.agent.PALMModelGenerator;

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
