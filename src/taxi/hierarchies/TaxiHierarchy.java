package taxi.hierarchies;

import burlap.mdp.singleagent.oo.OOSADomain;
import config.ExperimentConfig;
import config.taxi.TaxiConfig;
import hierarchy.framework.*;

public abstract class TaxiHierarchy extends Hierarchy {

    @Override
    public Task createHierarchy(ExperimentConfig experimentConfig, boolean plan) {
        TaxiConfig domain = (TaxiConfig) experimentConfig.domain;
        return createHierarchy(domain.correct_move, domain.fickle, plan);
    }

    public abstract Task createHierarchy(double correctMoveprob, double fickleProbability, boolean plan);

}