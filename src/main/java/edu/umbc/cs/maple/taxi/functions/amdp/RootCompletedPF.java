package edu.umbc.cs.maple.taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.hierarchy.framework.AMDPRootGoalPF;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.TaxiRootState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;


public class RootCompletedPF extends AMDPRootGoalPF {

    public RootCompletedPF() {
        super("root", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        if (!(s instanceof TaxiRootState)) { return false; }
        TaxiRootState st = (TaxiRootState) s;

        for(ObjectInstance passenger : st.objectsOfClass(CLASS_PASSENGER)) {
            String locationName = (String) passenger.get(ATT_LOCATION);
            String goalLocation = (String) passenger.get(ATT_GOAL_LOCATION);

            if(!locationName.equals(goalLocation)) {
                return false;
            }
        }
        return true;
    }

}
