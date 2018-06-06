package cleanup.hierarchies;

import burlap.mdp.singleagent.oo.OOSADomain;
import config.ExperimentConfig;
import hierarchy.framework.Hierarchy;
import hierarchy.framework.Task;

public abstract class CleanupHierarchy extends Hierarchy {

    protected OOSADomain baseDomain;

    public void setBaseDomain(OOSADomain baseDomain) {
        this.baseDomain = baseDomain;
    }

    public OOSADomain getBaseDomain(){
        return baseDomain;
    }

    public abstract Task createHierarchy(ExperimentConfig experimentConfig, boolean plan);

}