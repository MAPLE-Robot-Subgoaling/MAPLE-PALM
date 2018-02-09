package taxi.hierarchies.tasks.put.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.interfaces.ParameterizedStateMapping;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.state.TaxiState;

public class PutStateMapper implements ParameterizedStateMapping {

	//maps a base taxi state to L2
	@Override
	public State mapState(State s, String... params) {
		List<TaxiPutPassenger> passengers = new ArrayList<TaxiPutPassenger>();
		List<TaxiPutLocation> locations = new ArrayList<>();

		TaxiState st = (TaxiState) s;

		// Get Taxi
		String taxiLocation = TaxiPutDomain.ON_ROAD;
		int tx = (int)st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int)st.getTaxiAtt(Taxi.ATT_Y);
		for (String locName : st.getLocations()) {
			int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
			int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);

			locations.add(new TaxiPutLocation(locName));

			if (tx == lx && ty == ly) {
				taxiLocation = locName;
			}
		}
		TaxiPutAgent taxi = new TaxiPutAgent(Taxi.CLASS_TAXI, taxiLocation);

		for(String passengerName : params){
			String goal = (String) st.getPassengerAtt(passengerName, Taxi.ATT_GOAL_LOCATION);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			String location = "ERROR";
			if (inTaxi) {
				location = TaxiPutDomain.IN_TAXI;
			} else {
				int px = (int)st.getPassengerAtt(passengerName, Taxi.ATT_X);
				int py = (int)st.getPassengerAtt(passengerName, Taxi.ATT_Y);
				for (String locName : st.getLocations()) {
					int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
					int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);
					if (px == lx && py == ly) {
						location = locName;
					}
				}
			}
			if (location.equals("ERROR")) { throw new RuntimeException("ERROR: passenger at invalid location in mapper"); }
			passengers.add(new TaxiPutPassenger(passengerName, goal, location));
		}

		return new TaxiPutState(taxi, passengers, locations);
	}

}
