package cleanup.hierarchies;

import burlap.mdp.singleagent.oo.OOSADomain;

public abstract class CleanupHierarchy {

    protected OOSADomain baseDomain;

    public void setBaseDomain(OOSADomain baseDomain) {
        this.baseDomain = baseDomain;
    }

    public OOSADomain getBaseDomain(){
        return baseDomain;
    }

}