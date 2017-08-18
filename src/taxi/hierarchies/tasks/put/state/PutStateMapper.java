package taxi.hierarchies.tasks.put.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.state.TaxiState;

public class PutStateMapper implements StateMapping {
	//maps a base taxi state to L2
	@Override
	public State mapState(State s) {
		List<TaxiPutPassenger> passengers = new ArrayList<TaxiPutPassenger>();

		TaxiState st = (TaxiState) s;

		// Get Taxi
		String taxiLocation = TaxiPutDomain.ON_ROAD;
		int tx = (int)st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int)st.getTaxiAtt(Taxi.ATT_Y);
		for (String locName : st.getLocations()) {
			int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
			int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);

			if (tx == lx && ty == ly) {
				taxiLocation = locName;
				break;
			}
		}
		TaxiPutAgent taxi = new TaxiPutAgent(TaxiPutDomain.CLASS_TAXI, taxiLocation);

		for(String passengerName : st.getPassengers()){
			String goal = (String) st.getPassengerAtt(passengerName, Taxi.ATT_GOAL_LOCATION);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			passengers.add(new TaxiPutPassenger(passengerName, goal, inTaxi));
		}

		return new TaxiPutState(taxi, passengers);
	}

}
