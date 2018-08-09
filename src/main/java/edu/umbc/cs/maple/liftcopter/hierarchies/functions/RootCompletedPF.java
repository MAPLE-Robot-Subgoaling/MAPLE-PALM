package edu.umbc.cs.maple.liftcopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.hierarchy.framework.AMDPRootGoalPF;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.root.state.LCRootState;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;


public class RootCompletedPF extends AMDPRootGoalPF {

    public RootCompletedPF() {
        super("rootCompletedPF", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCRootState)) { return false; }
        LCRootState st = (LCRootState) s;

        for(ObjectInstance cargo : st.objectsOfClass(CLASS_CARGO)){
            String locationName = (String) cargo.get(ATT_LOCATION);
            String goalLocation = (String) cargo.get(ATT_GOAL_LOCATION);

            if(!locationName.equals(goalLocation)) {
                return false;
            }
        }
        return true;
    }

}
