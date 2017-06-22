package taxi.abstraction1;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.abstraction1.state.TaxiL1State;

public class TaxiL1TerminalFunction implements TerminalFunction {

	@Override
	public boolean isTerminal(State s) {
		TaxiL1State state = (TaxiL1State) s;
		
		for(String passengerName : state.getPassengers()){
			String location = (String) state.getPassengerAtt(passengerName, TaxiL1.ATT_CURRENT_LOCATION);
			String goalLocation = (String) state.getPassengerAtt(passengerName, TaxiL1.ATT_GOAL_LOCATION);
			if(!location.equals(goalLocation))
				return false;
			
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
			boolean pickedUp = (boolean) state.getPassengerAtt(passengerName, TaxiL1.ATT_PICKED_UP_AT_LEAST_ONCE);
			if(inTaxi || !pickedUp)
				return false;
		}
		return true;
	}

}
