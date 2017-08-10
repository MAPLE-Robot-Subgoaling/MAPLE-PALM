package taxi.abstraction2;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.abstraction2.state.TaxiL2State;

public class TaxiL2TerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
		
	@Override
	public boolean isTerminal(State s) {
		TaxiL2State state = (TaxiL2State) s;
		
		for(String passengerName : state.getPassengers()){
			String location = (String) state.getPassengerAtt(passengerName, TaxiL2.ATT_CURRENT_LOCATION);
			String goalLocation = (String) state.getPassengerAtt(passengerName, TaxiL2.ATT_GOAL_LOCATION);
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiL2.ATT_IN_TAXI);
			if(!location.equals(goalLocation) || inTaxi)
				return false;
		}
		return true;
	}

}
