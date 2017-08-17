package taxi.hierarchies.tasks.put.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

public class PutStateMapper implements StateMapping {
	//maps a base taxi state to L2
	@Override
	public State mapState(State s) {
		List<TaxiPutPassenger> passengers = new ArrayList<TaxiPutPassenger>();
		List<TaxiPutLocation> locations = new ArrayList<TaxiPutLocation>();
		
		TaxiState st = (TaxiState) s;

		for(String locName : st.getLocations()){
			String color = (String) st.getLocationAtt(locName, Taxi.ATT_COLOR);
			locations.add(new TaxiPutLocation(locName, color));
		}
		
		for(String passengerName : st.getPassengers()){
			int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			String currentLocation = "";
			
			for(String locName : st.getLocations()){
				int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
				int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);
				
				if(px == lx && py == ly){
					currentLocation = locName;
				}
			}
			passengers.add(new TaxiPutPassenger(passengerName, currentLocation, inTaxi));
		}

		return new TaxiPutState(passengers, locations);
	}

}
