package taxi.abstraction2.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

public class L2StateMapper implements StateMapping {
	//maps a base taxi state to L2
	@Override
	public State mapState(State s) {
		List<TaxiL2Passenger> passengers = new ArrayList<TaxiL2Passenger>();
		List<TaxiL2Location> locations = new ArrayList<TaxiL2Location>();
		
		TaxiState st = (TaxiState) s;

		for(String locName : st.getLocations()){
			String color = (String) st.getLocationAtt(locName, Taxi.ATT_COLOR);
			locations.add(new TaxiL2Location(locName, color));
		}
		
		for(String passengerName : st.getPassengers()){
			String goalLocation = (String) st.getPassengerAtt(passengerName, Taxi.ATT_GOAL_LOCATION);
			int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
			boolean inTax = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			boolean justpickup = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_JUST_PICKED_UP);
			String currentLocation = "";
			
			for(String locName : st.getLocations()){
				int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
				int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);
				
				if(px == lx && py == ly){
					currentLocation = locName;
				}
			}
			passengers.add(new TaxiL2Passenger(passengerName, currentLocation, goalLocation, inTax, justpickup));
		}

		return new TaxiL2State(passengers, locations);
	}

}
