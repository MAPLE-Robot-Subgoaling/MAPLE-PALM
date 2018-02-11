package taxi.hierarchies.tasks.bringon.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.interfaces.ParameterizedStateMapping;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.state.TaxiState;

public class BringonStateMapper implements ParameterizedStateMapping {
	//projection function from the base taxi to abstraction 1
	
	@Override
	public State mapState(State s, String... params) {
		List<TaxiBringonPassenger> passengers = new ArrayList<TaxiBringonPassenger>();
		TaxiState st = (TaxiState) s;

		// Get Taxi
		String taxiLocation = TaxiBringonDomain.ON_ROAD;
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
		TaxiBringonAgent taxi = new TaxiBringonAgent(Taxi.CLASS_TAXI, taxiLocation);

		// Get Passengers
		for(String passengerName : params){
//		for(String passengerName : st.getPassengers()){
			int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			String passengerLocation = TaxiBringonDomain.IN_TAXI;

			if(!inTaxi) {
				for (String locName : st.getLocations()) {
					int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
					int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);

					if (px == lx && py == ly) {
						passengerLocation = locName;
						break;
					}
				}
			}
			passengers.add(new TaxiBringonPassenger(passengerName, passengerLocation));
		}
		return new TaxiBringonState(taxi, passengers);
	}

}
