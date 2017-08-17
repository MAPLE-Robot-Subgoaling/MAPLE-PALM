package taxi.hierarchies.tasks.bringon.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.state.TaxiState;

public class BringonStateMapper implements StateMapping {
	//projection function from the base taxi to abstraction 1
	
	@Override
	public State mapState(State s) {
		List<TaxiBringonPassenger> passengers = new ArrayList<TaxiBringonPassenger>();
		List<TaxiBringonLocation> locations = new ArrayList<TaxiBringonLocation>();
		
		TaxiState st = (TaxiState) s;

		String taxiLocation = TaxiBringonDomain.ON_ROAD;
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		
		for(String locName : st.getLocations()){
			String color = (String) st.getLocationAtt(locName, Taxi.ATT_COLOR);
			locations.add(new TaxiBringonLocation(locName, color));
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
				if(tx == lx && ty == ly){
					taxiLocation = locName;
				}
			}
			passengers.add(new TaxiBringonPassenger(passengerName, currentLocation, inTaxi));
		}
		
		boolean taxiOccupied = (boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);
		String Tname = st.getTaxiName();
		
		TaxiBringonAgent taxi = new TaxiBringonAgent(Tname, taxiLocation);
		
		return new TaxiBringonState(taxi, passengers, locations);
	}

}
