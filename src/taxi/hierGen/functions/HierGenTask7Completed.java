package taxi.hierGen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierGen.Task7.state.TaxiHierGenTask7State;

import static taxi.TaxiConstants.ATT_IN_TAXI;

public class HierGenTask7Completed extends PropositionalFunction {

    public HierGenTask7Completed(){
        super("Task 7", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        //p.in taxi = true
        TaxiHierGenTask7State st = (TaxiHierGenTask7State) s;

        for(String pname : st.getPassengers()){
            boolean inTaxi = (boolean) st.getPassengerAtt(pname, ATT_IN_TAXI);
            if(inTaxi)
                return true;
        }
        return false;
    }
}
