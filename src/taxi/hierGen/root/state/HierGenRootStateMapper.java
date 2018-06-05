package taxi.hierGen.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class HierGenRootStateMapper implements StateMapping {
    @Override
    public State mapState(State s) {
        TaxiState st = (TaxiState) s;

        int tx = (int) st.getTaxi().get(ATT_X);
        int ty = (int) st.getTaxi().get(ATT_Y);
        TaxiHierGenRootTaxi taxi = new TaxiHierGenRootTaxi(st.getTaxiName(), tx, ty);

        List<TaxiHierGenRootPassenger> passengers = new ArrayList<TaxiHierGenRootPassenger>();
        for(ObjectInstance passenger : st.objectsOfClass(CLASS_PASSENGER)){
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);

            String goalLoc = (String) passenger.get(ATT_GOAL_LOCATION);
            int destX = (int) st.object(goalLoc).get(ATT_X);
            int destY = (int) st.object(goalLoc).get(ATT_Y);


            passengers.add(new TaxiHierGenRootPassenger(passenger.name(), px, py, destX, destY, inTaxi));
        }

        return new TaxiHierGenRootState(taxi, passengers);
    }
}
