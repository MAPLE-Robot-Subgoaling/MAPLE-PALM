package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.root.state.TaxiRootState;

import static taxi.TaxiConstants.ATT_GOAL_LOCATION;
import static taxi.TaxiConstants.ATT_LOCATION;
import static taxi.TaxiConstants.CLASS_PASSENGER;


public class RootCompletedPF extends PropositionalFunction {

    public RootCompletedPF() {
        super("root", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
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
