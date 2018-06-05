package taxi.hierarchies;

import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.*;

public abstract class TaxiHierarchy extends Hierarchy {

    /**
     * the full base taxi domain
     */
    public static OOSADomain baseDomain;

    /**
     * get base taxi domain
     * @return full base taxi domain
     */
    public static OOSADomain getBaseDomain(){
        return baseDomain;
    }

    public abstract Task createHierarchy(double correctMoveprob, double fickleProbability, boolean plan);

}