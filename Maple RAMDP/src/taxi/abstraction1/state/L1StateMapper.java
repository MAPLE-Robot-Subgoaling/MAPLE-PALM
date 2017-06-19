package taxi.abstraction1.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.abstraction1.TaxiL1;
import taxi.state.TaxiAgent;
import taxi.state.TaxiState;

public class L1StateMapper implements StateMapping {

	@Override
	public State mapState(State s) {
		List<TaxiL1Passenger> passengers = new ArrayList<TaxiL1Passenger>();
		List<TaxiL1Location> locations = new ArrayList<TaxiL1Location>();
		
		TaxiState st = (TaxiState) s;

		String taxiLocation = TaxiL1.ON_ROAD;
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		
		for(String locName : st.getLocations()){
			String color = (String) st.getLocationAtt(locName, Taxi.ATT_COLOR);
			locations.add(new TaxiL1Location(locName, color));
		}
		
		for(String passengerName : st.getPassengers()){
			String goalLocation = (String) st.getPassengerAtt(passengerName, Taxi.ATT_GOAL_LOCATION);
			int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
			boolean inTax = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			boolean pickedUp = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_PICKED_UP_AT_LEAST_ONCE);
			boolean justpickup = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_JUST_PICKED_UP);
			String currentLocation = "";
			
			for(String locName : st.getLocations()){
				int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
				int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);
				
				if(px == lx && py == ly){
					currentLocation = locName;
				}
				if(tx == lx && ty == ly){
					taxiLocation = locName;
				}
			}
			passengers.add(new TaxiL1Passenger(passengerName, currentLocation, goalLocation, inTax, pickedUp, justpickup));
		}
		
		boolean taxiOccupied = (boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);
		String Tname = st.getTaxiName();
		
		TaxiL1Agent taxi = new TaxiL1Agent(Tname, taxiLocation, taxiOccupied);
		
		return new TaxiL1State(taxi, passengers, locations);
	}

}
