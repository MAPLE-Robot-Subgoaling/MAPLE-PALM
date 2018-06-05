package cleanup.hierarchies;

import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.Hierarchy;

public abstract class CleanupHierarchy extends Hierarchy {

    protected OOSADomain baseDomain;

    public void setBaseDomain(OOSADomain baseDomain) {
        this.baseDomain = baseDomain;
    }

    public OOSADomain getBaseDomain(){
        return baseDomain;
    }

}