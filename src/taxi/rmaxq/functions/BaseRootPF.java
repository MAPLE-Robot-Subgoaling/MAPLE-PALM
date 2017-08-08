package taxi.rmaxq.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BaseRootPF  extends PropositionalFunction{

	public BaseRootPF (){
		super("root", new String[]{});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState state = (TaxiState) s;

		for(String passengerName : state.getPassengers()) {
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			boolean pickedUpOnce = (boolean) state.getPassengerAtt(passengerName,
					Taxi.ATT_PICKED_UP_AT_LEAST_ONCE);
			if (inTaxi || !pickedUpOnce)
				return false;

			String passengerGoal = (String) state.getPassengerAtt(passengerName, Taxi.ATT_GOAL_LOCATION);
			int px = (int) state.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) state.getPassengerAtt(passengerName, Taxi.ATT_Y);

			for (String locName : state.getLocations()) {
				if (passengerGoal.equals(locName)) {
					int lx = (int) state.getLocationAtt(locName, Taxi.ATT_X);
					int ly = (int) state.getLocationAtt(locName, Taxi.ATT_Y);
					if (lx != px || ly != py)
						return false;

					break;
				}
			}
		}
		return true;
	}
}
