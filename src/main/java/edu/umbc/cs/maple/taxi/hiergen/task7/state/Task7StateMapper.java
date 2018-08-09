package edu.umbc.cs.maple.taxi.hiergen.task7.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.liftcopter.hierarchies.interfaces.ParameterizedStateMapping;
import edu.umbc.cs.maple.taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;


public class Task7StateMapper implements ParameterizedStateMapping {
    @Override
    public State mapState(State s, String... params) {
        TaxiState st = (TaxiState) s;

        int tx = (int) st.getTaxi().get(ATT_X);
        int ty = (int) st.getTaxi().get(ATT_Y);
        TaxiHierGenTask7Taxi taxi = new TaxiHierGenTask7Taxi(st.getTaxiName(), tx, ty);

        List<TaxiHierGenTask7Passenger> passengers = new ArrayList<TaxiHierGenTask7Passenger>();
        for(String passengerName : params){
            ObjectInstance passenger = st.object(passengerName);
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);

            passengers.add(new TaxiHierGenTask7Passenger(passenger.name(), px, py, inTaxi));
        }

        return new TaxiHierGenTask7State(taxi, passengers);
    }
}
