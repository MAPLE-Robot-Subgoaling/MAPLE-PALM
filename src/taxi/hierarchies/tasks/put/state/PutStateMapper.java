package taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import taxi.hierarchies.interfaces.MaskedParameterizedStateMapping;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class PutStateMapper implements MaskedParameterizedStateMapping {

    public static final String PUT_PASSENGER_ALIAS = "**PUT_PASSENGER_ALIAS**";

    @Override
    public String[] getMaskedParameters() {
        return new String[]{CLASS_PASSENGER};
    }
    //maps a base taxi state to L2
    @Override
    public State mapState(State s, String... params) {
        List<TaxiPutPassenger> passengers = new ArrayList<TaxiPutPassenger>();
        List<TaxiPutLocation> locations = new ArrayList<>();

        TaxiState st = (TaxiState) s;

        // Get Taxi
        String taxiLocation = ATT_VAL_ON_ROAD;
        int tx = (int) st.getTaxi().get(ATT_X);
        int ty = (int) st.getTaxi().get(ATT_Y);
        for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
            int lx = (int) location.get(ATT_X);
            int ly = (int) location.get(ATT_Y);

            locations.add(new TaxiPutLocation(location.name()));

            if (tx == lx && ty == ly) {
                taxiLocation = location.name();
            }
        }
        TaxiPutAgent taxi = new TaxiPutAgent(CLASS_TAXI, taxiLocation);

        for (String passengerName : params) {
            TaxiPassenger passenger = (TaxiPassenger) st.object(passengerName);
            String goal = (String) passenger.get(ATT_GOAL_LOCATION);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
            String location = ERROR;
            if (inTaxi) {
                location = ATT_VAL_IN_TAXI;
            } else {
                int px = (int) passenger.get(ATT_X);
                int py = (int) passenger.get(ATT_Y);
                for (ObjectInstance otherLocation : st.objectsOfClass(CLASS_LOCATION)) {
                    int lx = (int) otherLocation.get(ATT_X);
                    int ly = (int) otherLocation.get(ATT_Y);
                    if (px == lx && py == ly) {
                        location = otherLocation.name();
                    }
                }
            }
            if (location.equals(ERROR)) {
                throw new RuntimeException("Error: passenger at invalid location in mapper");
            }
            passengers.add(new TaxiPutPassenger(PUT_PASSENGER_ALIAS, goal, location));
//            passengers.add(new TaxiPutPassenger(passengerName, goal, location));
        }

        return new TaxiPutState(taxi, passengers, locations);
    }
}
