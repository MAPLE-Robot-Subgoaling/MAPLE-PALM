package taxi.hierGen.Task7.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

public class HierGenTask7StateMapper implements StateMapping {
	@Override
	public State mapState(State s) {
		TaxiState st = (TaxiState) s;

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_X);
		TaxiHierGenTask7Taxi taxi = new TaxiHierGenTask7Taxi(st.getTaxiName(), tx, ty);

		List<TaxiHierGenTask7Passenger> passengers = new ArrayList<TaxiHierGenTask7Passenger>();
		for(String pnam : st.getPassengers()){
			int px = (int) st.getPassengerAtt(pnam, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(pnam, Taxi.ATT_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(pnam, Taxi.ATT_IN_TAXI);

			passengers.add(new TaxiHierGenTask7Passenger(pnam, px, py, inTaxi));
		}

		return new TaxiHierGenTask7State(taxi, passengers);
	}
}
