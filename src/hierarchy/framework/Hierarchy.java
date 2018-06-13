package hierarchy.framework;

import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import config.ExperimentConfig;

public abstract class Hierarchy {

    public static void setupKnownTFRF(NonprimitiveTask task) {
        GoalFailTF tf = task.getGoalFailTF();
        GoalFailRF rf = task.getGoalFailRF();
        FactoredModel model = (FactoredModel) task.getDomain().getModel();
        model.setTf(tf);
        model.setRf(rf);
    }

    protected OOSADomain baseDomain;

    public OOSADomain getBaseDomain(){
        return baseDomain;
    }

    public void setBaseDomain(OOSADomain baseDomain) {
        this.baseDomain = baseDomain;
    }

    public abstract Task createHierarchy(ExperimentConfig experimentConfig, boolean plan);

}
