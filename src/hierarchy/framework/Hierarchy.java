package hierarchy.framework;

import burlap.mdp.singleagent.model.FactoredModel;

public abstract class Hierarchy {

    public static void setupKnownTFRF(NonprimitiveTask task) {
        GoalFailTF tf = task.getGoalFailTF();
        GoalFailRF rf = task.getGoalFailRF();
        FactoredModel model = (FactoredModel) task.getDomain().getModel();
        model.setTf(tf);
        model.setRf(rf);
    }



}
