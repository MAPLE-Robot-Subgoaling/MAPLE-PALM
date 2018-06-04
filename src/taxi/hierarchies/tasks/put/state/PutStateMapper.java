package taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.state.State;
import taxi.hierarchies.interfaces.ParameterizedStateMapping;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class PutStateMapper implements ParameterizedStateMapping {

    //maps a base taxi state to L2
    @Override
    public State mapState(State s, String... params) {
        List<TaxiPutPassenger> passengers = new ArrayList<TaxiPutPassenger>();
        List<TaxiPutLocation> locations = new ArrayList<>();

        TaxiState st = (TaxiState) s;

        // Get Taxi
        String taxiLocation = ATT_VAL_ON_ROAD;
        int tx = (int)st.getTaxiAtt(ATT_X);
        int ty = (int)st.getTaxiAtt(ATT_Y);
        for (String locName : st.getLocations()) {
            int lx = (int) st.getLocationAtt(locName, ATT_X);
            int ly = (int) st.getLocationAtt(locName, ATT_Y);

            locations.add(new TaxiPutLocation(locName));

            if (tx == lx && ty == ly) {
                taxiLocation = locName;
            }
        }
        TaxiPutAgent taxi = new TaxiPutAgent(CLASS_TAXI, taxiLocation);

        for(String passengerName : params){
//		for(String passengerName : st.getPassengers()) {
            String goal = (String) st.getPassengerAtt(passengerName, ATT_GOAL_LOCATION);
            boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, ATT_IN_TAXI);
            String location = ERROR;
            if (inTaxi) {
                location = ATT_VAL_IN_TAXI;
            } else {
                int px = (int)st.getPassengerAtt(passengerName, ATT_X);
                int py = (int)st.getPassengerAtt(passengerName, ATT_Y);
                for (String locName : st.getLocations()) {
                    int lx = (int) st.getLocationAtt(locName, ATT_X);
                    int ly = (int) st.getLocationAtt(locName, ATT_Y);
                    if (px == lx && py == ly) {
                        location = locName;
                    }
                }
            }
            if (location.equals(ERROR)) { throw new RuntimeException("Error: passenger at invalid location in mapper"); }
            passengers.add(new TaxiPutPassenger(passengerName, goal, location));
        }

        return new TaxiPutState(taxi, passengers, locations);
    }

}
