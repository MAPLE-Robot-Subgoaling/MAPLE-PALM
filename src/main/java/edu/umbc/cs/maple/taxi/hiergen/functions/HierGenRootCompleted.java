
package edu.umbc.cs.maple.taxi.hiergen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.taxi.hiergen.root.state.TaxiHierGenRootState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class HierGenRootCompleted extends PropositionalFunction {

    public HierGenRootCompleted(){
        super("root", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        TaxiHierGenRootState st = (TaxiHierGenRootState) s;
        if (st.getTaxi() == null) {
            // if no taxi, assume it is an RMAX imagined state, return true
            return true;
        }
        for(ObjectInstance passenger : st.objectsOfClass(CLASS_PASSENGER)){
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            int destX = (int) passenger.get(ATT_DESTINATION_X);
            int destY = (int) passenger.get(ATT_DESTINATION_Y);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
            if (inTaxi || px != destX || py != destY) {
                return false;
            }
        }
        return true;

    }
}
