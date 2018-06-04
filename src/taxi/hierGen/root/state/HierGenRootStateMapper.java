package taxi.hierGen.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class HierGenRootStateMapper implements StateMapping {
    @Override
    public State mapState(State s) {
        TaxiState st = (TaxiState) s;

        int tx = (int) st.getTaxiAtt(ATT_X);
        int ty = (int) st.getTaxiAtt(ATT_Y);
        TaxiHierGenRootTaxi taxi = new TaxiHierGenRootTaxi(st.getTaxiName(), tx, ty);

        List<TaxiHierGenRootPassenger> passengers = new ArrayList<TaxiHierGenRootPassenger>();
        for(String pnam : st.getPassengers()){
            int px = (int) st.getPassengerAtt(pnam, ATT_X);
            int py = (int) st.getPassengerAtt(pnam, ATT_Y);
            boolean inTaxi = (boolean) st.getPassengerAtt(pnam, ATT_IN_TAXI);

            String goalLoc = (String) st.getPassengerAtt(pnam, ATT_GOAL_LOCATION);
            int destX = (int) st.getLocationAtt(goalLoc, ATT_X);
            int destY = (int) st.getLocationAtt(goalLoc, ATT_Y);


            passengers.add(new TaxiHierGenRootPassenger(pnam, px, py, destX, destY, inTaxi));
        }

        return new TaxiHierGenRootState(taxi, passengers);
    }
}
