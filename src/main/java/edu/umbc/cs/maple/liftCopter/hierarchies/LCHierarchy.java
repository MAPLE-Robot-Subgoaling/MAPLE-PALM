package edu.umbc.cs.maple.liftCopter.hierarchies;

import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.hierarchy.framework.Hierarchy;
import edu.umbc.cs.maple.hierarchy.framework.Task;

public abstract class LCHierarchy extends Hierarchy {
        /**
         * the full base taxi domain
         */
        public static OOSADomain baseDomain;

        /**
         * get base taxi domain
         * @return full base taxi domain
         */
        public OOSADomain getBaseDomain(){
            return baseDomain;
        }

        public abstract Task createHierarchy(double correctMoveprob, boolean plan);
}
