package palm.rmax.agent;

import burlap.statehashing.HashableStateFactory;
import config.ExperimentConfig;
import hierarchy.framework.GroundedTask;
import palm.agent.PALMModel;
import palm.agent.PALMModelGenerator;

public class PALMRmaxModelGenerator implements PALMModelGenerator {

    private int threshold;
    private double rmax;
    private double gamma;
    private boolean useMultitimeModel;
    private HashableStateFactory hashingFactory;

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
        return new HierarchicalRmaxModel(t, this.threshold, this.rmax,
                this.hashingFactory, this.gamma, this.useMultitimeModel);
    }
}
