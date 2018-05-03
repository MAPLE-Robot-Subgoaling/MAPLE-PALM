package taxi.hierarchies.tasks.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;
import static taxi.TaxiConstants.*;

public class RootStateMapper implements StateMapping {
	@Override
	public State mapState(State s) {
		List<TaxiRootPassenger> passengers = new ArrayList<>();
		TaxiState st = (TaxiState) s;

		for(String passengerName : st.getPassengers()){
			int px = (int) st.getPassengerAtt(passengerName, ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, ATT_Y);
			String goalLocation = (String)st.getPassengerAtt(passengerName, ATT_GOAL_LOCATION);
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, ATT_IN_TAXI);

			if(inTaxi) {
				passengers.add(new TaxiRootPassenger(passengerName, ATT_VAL_IN_TAXI, goalLocation));
			} else {
				for(String locName : st.getLocations()){
					int lx = (int) st.getLocationAtt(locName, ATT_X);
					int ly = (int) st.getLocationAtt(locName, ATT_Y);

					if(px == lx && py == ly){
						passengers.add(new TaxiRootPassenger(passengerName, locName, goalLocation));
					}
				}
			}
		}

		return new TaxiRootState(passengers);
	}

}
