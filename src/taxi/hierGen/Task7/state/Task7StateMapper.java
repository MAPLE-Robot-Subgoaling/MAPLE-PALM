package taxi.hierGen.Task7.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;


public class Task7StateMapper implements StateMapping {
	@Override
	public State mapState(State s) {
		TaxiState st = (TaxiState) s;

		int tx = (int) st.getTaxiAtt(ATT_X);
		int ty = (int) st.getTaxiAtt(ATT_Y);
		TaxiHierGenTask7Taxi taxi = new TaxiHierGenTask7Taxi(st.getTaxiName(), tx, ty);

		List<TaxiHierGenTask7Passenger> passengers = new ArrayList<TaxiHierGenTask7Passenger>();
		for(String pnam : st.getPassengers()){
			int px = (int) st.getPassengerAtt(pnam, ATT_X);
			int py = (int) st.getPassengerAtt(pnam, ATT_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(pnam, ATT_IN_TAXI);

			passengers.add(new TaxiHierGenTask7Passenger(pnam, px, py, inTaxi));
		}

		return new TaxiHierGenTask7State(taxi, passengers);
	}
}
