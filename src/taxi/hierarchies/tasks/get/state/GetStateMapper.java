package taxi.hierarchies.tasks.get.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.state.State;
import taxi.hierarchies.interfaces.ParameterizedStateMapping;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.state.TaxiState;
import taxi.Taxi;
import static taxi.TaxiConstants.*;
import static taxi.TaxiConstants.ATT_VAL_ON_ROAD;

public class GetStateMapper implements ParameterizedStateMapping {
	//maps a base taxi state to L2
	@Override
	public State mapState(State s, String...params) {
		List<TaxiGetPassenger> passengers = new ArrayList<TaxiGetPassenger>();
		List<TaxiGetLocation> locations = new ArrayList<TaxiGetLocation>();
		TaxiState st = (TaxiState) s;

		// Get Taxi
        String taxiLocation = ATT_VAL_ON_ROAD;
		int tx = (int)st.getTaxiAtt(ATT_X);
		int ty = (int)st.getTaxiAtt(ATT_Y);
		for (String locName : st.getLocations()) {
			int lx = (int) st.getLocationAtt(locName, ATT_X);
			int ly = (int) st.getLocationAtt(locName, ATT_Y);

			locations.add(new TaxiGetLocation(locName));

			if (tx == lx && ty == ly) {
				taxiLocation = locName;
			}
		}
		TaxiGetAgent taxi = new TaxiGetAgent(CLASS_TAXI, taxiLocation);

		// Get Passengers
		for(String passengerName : params){
//		for(String passengerName : st.getPassengers()) {
			int px = (int) st.getPassengerAtt(passengerName, ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, ATT_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, ATT_IN_TAXI);
			String passengerLocation = ATT_VAL_IN_TAXI;

			if(!inTaxi) {
				for (String locName : st.getLocations()) {
					int lx = (int) st.getLocationAtt(locName, ATT_X);
					int ly = (int) st.getLocationAtt(locName, ATT_Y);

					if (px == lx && py == ly) {
						passengerLocation = locName;
						break;
					}
				}
			}
			passengers.add(new TaxiGetPassenger(passengerName, passengerLocation));
		}

		return new TaxiGetState(taxi, passengers, locations);
	}

}
