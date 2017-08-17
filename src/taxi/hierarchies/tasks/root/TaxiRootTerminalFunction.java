package taxi.hierarchies.tasks.root;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.root.state.TaxiRootState;

public class TaxiRootTerminalFunction implements TerminalFunction {
	@Override
	public boolean isTerminal(State s) {
		TaxiRootState state = (TaxiRootState) s;
		
		for(String passengerName : state.getPassengers()){
			String goalLocation = (String)state.getPassengerAtt(passengerName, TaxiRootDomain.ATT_GOAL_LOCATION);
			String currentLocation = (String)state.getPassengerAtt(passengerName, TaxiRootDomain.ATT_CURRENT_LOCATION);
			boolean inTaxi = (boolean)state.getPassengerAtt(passengerName, TaxiRootDomain.ATT_IN_TAXI);
			if(!goalLocation.equals(currentLocation) || inTaxi) return false;
		}
		return true;
	}

}
