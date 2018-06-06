
package taxi.hierGen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierGen.root.state.TaxiHierGenRootState;

import static taxi.TaxiConstants.*;

public class HierGenRootCompleted extends PropositionalFunction {

    public HierGenRootCompleted(){
        super("root", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        TaxiHierGenRootState st = (TaxiHierGenRootState) s;
        for(ObjectInstance passenger : st.objectsOfClass(CLASS_PASSENGER)){
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            int destX = (int) passenger.get(ATT_DESTINATION_X);
            int destY = (int) passenger.get(ATT_DESTINATION_Y);
//            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
//            if(inTaxi)
//                return false;
            if(px != destX || py != destY)
                return false;
        }
        return true;

    }
}
