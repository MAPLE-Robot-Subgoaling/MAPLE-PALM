package palm.rmax.agent;

import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import palm.agent.PALMModel;
import palm.agent.PALMModelGenerator;

public class ExpectedRmaxModelGenerator implements PALMModelGenerator {

    private int threshold;
    private double rmax;
    private double gamma;
    private boolean useMultitimeModel;
    private HashableStateFactory hashingFactory;

    public ExpectedRmaxModelGenerator( int threshold, double rmax, HashableStateFactory hs,
                                   double gamma, boolean useMultitimeModel) {
        this.threshold = threshold;
        this.rmax = rmax;
        this.hashingFactory = hs;
        this.gamma = gamma;
        this.useMultitimeModel = useMultitimeModel;
    }
    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        return new ExpectedRmaxModel(t, this.threshold, this.rmax,
                this.hashingFactory, this.gamma, this.useMultitimeModel);
    }
}
