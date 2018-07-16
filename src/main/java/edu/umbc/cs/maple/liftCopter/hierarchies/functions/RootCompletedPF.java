package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.LCRootState;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;


public class RootCompletedPF extends PropositionalFunction {

    public RootCompletedPF() {
        super("root", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCRootState)) { return false; }
        LCRootState st = (LCRootState) s;

        for(String passengerName : st.getCargos()){
            String locationName = (String) st.getCargoAtt(passengerName, ATT_LOCATION);
            String goalLocation = (String) st.getCargoAtt(passengerName, ATT_GOAL_LOCATION);

            if(!locationName.equals(goalLocation))
                return false;
        }
        return true;
    }

}
