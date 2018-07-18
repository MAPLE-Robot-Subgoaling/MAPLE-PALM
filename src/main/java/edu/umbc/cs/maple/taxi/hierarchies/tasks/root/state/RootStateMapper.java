package edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.TaxiPutLocation;
import edu.umbc.cs.maple.taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class RootStateMapper implements StateMapping {
    @Override
    public State mapState(State s) {
        List<TaxiRootPassenger> passengers = new ArrayList<>();
        TaxiState st = (TaxiState) s;

        String taxiLocation = ATT_VAL_ON_ROAD;
        int tx = (int) st.getTaxi().get(ATT_X);
        int ty = (int) st.getTaxi().get(ATT_Y);
        for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
            int lx = (int) location.get(ATT_X);
            int ly = (int) location.get(ATT_Y);
            if (tx == lx && ty == ly) {
                taxiLocation = location.name();
            }
        }
        TaxiRootAgent taxi = new TaxiRootAgent(CLASS_TAXI, taxiLocation);
        for(ObjectInstance passenger : st.objectsOfClass(CLASS_PASSENGER)){
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            String goalLocation = (String) passenger.get(ATT_GOAL_LOCATION);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);

            if(inTaxi) {
                passengers.add(new TaxiRootPassenger(passenger.name(), ATT_VAL_IN_TAXI, goalLocation));
            } else {
                for(ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)){
                    int lx = (int) location.get(ATT_X);
                    int ly = (int) location.get(ATT_Y);

                    if(px == lx && py == ly){
                        passengers.add(new TaxiRootPassenger(passenger.name(), location.name(), goalLocation));
                    }
                }
            }
        }

        return new TaxiRootState(taxi, passengers);
    }

}
