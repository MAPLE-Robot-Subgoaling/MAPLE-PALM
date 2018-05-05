package palm.rmax.agent;

import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import palm.agent.PALMModel;
import palm.agent.PALMModelGenerator;

public class PALMRmaxModelGenerator implements PALMModelGenerator {

    private int threshold;
    private double rmax;
    private double gamma;
    private boolean useMultitimeModel;
    private HashableStateFactory hashingFactory;

    public PALMRmaxModelGenerator( int threshold, double rmax, HashableStateFactory hs,
        double gamma, boolean useMultitimeModel) {
        this.threshold = threshold;
        this.rmax = rmax;
        this.hashingFactory = hs;
        this.gamma = gamma;
        this.useMultitimeModel = useMultitimeModel;
    }
    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        return new HierarchicalRmaxModel(t, this.threshold, this.rmax,
                this.hashingFactory, this.gamma, this.useMultitimeModel);
    }
}
