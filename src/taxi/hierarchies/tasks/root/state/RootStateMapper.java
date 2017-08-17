package taxi.hierarchies.tasks.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

public class RootStateMapper implements StateMapping {
	//maps a base taxi state to L2
	@Override
	public State mapState(State s) {
		List<TaxiRootPassenger> passengers = new ArrayList<TaxiRootPassenger>();
		List<TaxiRootLocation> locations = new ArrayList<TaxiRootLocation>();
		
		TaxiState st = (TaxiState) s;

		for(String locName : st.getLocations()){
			String color = (String) st.getLocationAtt(locName, Taxi.ATT_COLOR);
			locations.add(new TaxiRootLocation(locName, color));
		}
		
		for(String passengerName : st.getPassengers()){
			int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
			String goalLocation = (String)st.getPassengerAtt(passengerName, Taxi.ATT_GOAL_LOCATION);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			String currentLocation = "";
			
			for(String locName : st.getLocations()){
				int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
				int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);
				
				if(px == lx && py == ly){
					currentLocation = locName;
				}
			}
			passengers.add(new TaxiRootPassenger(passengerName, currentLocation, goalLocation, inTaxi));
		}

		return new TaxiRootState(passengers, locations);
	}

}
