package taxi.hierGen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierGen.Task7.state.TaxiHierGenTask7State;

import static taxi.TaxiConstants.ATT_IN_TAXI;
import static taxi.TaxiConstants.PF_TASK_7;

public class HierGenTask7Completed extends PropositionalFunction {

    public HierGenTask7Completed(){
        super(PF_TASK_7, new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        TaxiHierGenTask7State st = (TaxiHierGenTask7State) s;
        for(String pname : params){
            boolean inTaxi = (boolean) st.object(pname).get(ATT_IN_TAXI);
            if(inTaxi)
                return true;
        }
        return false;
//        throw new RuntimeException("this is wrong, need to reimplement");
    }
}
