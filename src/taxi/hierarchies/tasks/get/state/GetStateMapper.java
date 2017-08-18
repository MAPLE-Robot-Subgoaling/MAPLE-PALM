package taxi.hierarchies.tasks.get.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.state.TaxiState;

public class GetStateMapper implements StateMapping {
	//maps a base taxi state to L2
	@Override
	public State mapState(State s) {
		List<TaxiGetPassenger> passengers = new ArrayList<TaxiGetPassenger>();
		TaxiState st = (TaxiState) s;

		// Get Taxi
        String taxiLocation = TaxiGetDomain.ON_ROAD;
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
		TaxiGetAgent taxi = new TaxiGetAgent(TaxiGetDomain.CLASS_TAXI, taxiLocation);

		// Get Passengers
		for(String passengerName : st.getPassengers()){
			int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			String passengerLocation = TaxiGetDomain.IN_TAXI;

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
			passengers.add(new TaxiGetPassenger(passengerName, passengerLocation));
		}

		return new TaxiGetState(taxi, passengers);
	}

}
