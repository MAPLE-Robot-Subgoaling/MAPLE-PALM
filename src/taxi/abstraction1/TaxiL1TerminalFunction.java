package taxi.abstraction1;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.abstraction1.state.TaxiL1State;

public class TaxiL1TerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
	
	@Override
	public boolean isTerminal(State s) {
		TaxiL1State state = (TaxiL1State) s;
		
		for(String passengerName : state.getPassengers()){
			String location = (String) state.getPassengerAtt(passengerName, TaxiL1.ATT_CURRENT_LOCATION);
			String goalLocation = (String) state.getPassengerAtt(passengerName, TaxiL1.ATT_GOAL_LOCATION);
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
			if(!location.equals(goalLocation) || inTaxi)
				return false;
		}
		return true;
	}

}
