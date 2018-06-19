package edu.umbc.cs.maple.taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.TaxiRootState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_GOAL_LOCATION;
import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_LOCATION;


public class RootCompletedPF extends PropositionalFunction {

    public RootCompletedPF() {
        super("root", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof TaxiRootState)) { return false; }
        TaxiRootState st = (TaxiRootState) s;

        for(String passengerName : st.getPassengers()){
            String locationName = (String) st.getPassengerAtt(passengerName, ATT_LOCATION);
            String goalLocation = (String) st.getPassengerAtt(passengerName, ATT_GOAL_LOCATION);

            if(!locationName.equals(goalLocation))
                return false;
        }
        return true;
    }

}
