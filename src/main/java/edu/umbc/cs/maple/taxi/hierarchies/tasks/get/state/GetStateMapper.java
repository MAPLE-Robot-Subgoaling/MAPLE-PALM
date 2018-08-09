package edu.umbc.cs.maple.taxi.hierarchies.tasks.get.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.utilities.MaskedParameterizedStateMapping;
import edu.umbc.cs.maple.taxi.state.TaxiPassenger;
import edu.umbc.cs.maple.taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class GetStateMapper implements MaskedParameterizedStateMapping {

    public static final String GET_PASSENGER_ALIAS = "**GET_PASSENGER_ALIAS**";

    @Override
    public String[] getMaskedParameters()
    {
        return new String[]{GET_PASSENGER_ALIAS};
    }
    //maps a base taxi state to L2
    @Override
    public State mapState(State s, String... params) {
        List<TaxiGetPassenger> passengers = new ArrayList<TaxiGetPassenger>();
        List<TaxiGetLocation> locations = new ArrayList<TaxiGetLocation>();
        TaxiState st = (TaxiState) s;

        // Get Taxi
        String taxiLocation = ATT_VAL_ON_ROAD;
        int tx = (int) st.getTaxi().get(ATT_X);
        int ty = (int) st.getTaxi().get(ATT_Y);
        for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
            int lx = (int) location.get(ATT_X);
            int ly = (int) location.get(ATT_Y);

            locations.add(new TaxiGetLocation(location.name()));

            if (tx == lx && ty == ly) {
                taxiLocation = location.name();
            }
        }
        TaxiGetAgent taxi = new TaxiGetAgent(CLASS_TAXI, taxiLocation);

        // Get Passengers
        for (String passengerName : params) {
            TaxiPassenger passenger = (TaxiPassenger) st.object(passengerName);
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
            String passengerLocation = ATT_VAL_IN_TAXI;

            if (!inTaxi) {
                for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
                    int lx = (int) location.get(ATT_X);
                    int ly = (int) location.get(ATT_Y);

                    if (px == lx && py == ly) {
                        passengerLocation = location.name();
                        break;
                    }
                }
            }
//            passengers.add(new TaxiGetPassenger(passengerName, passengerLocation));
            passengers.add(new TaxiGetPassenger(GET_PASSENGER_ALIAS, passengerLocation));
        }

        return new TaxiGetState(taxi, passengers, locations);
    }
}
