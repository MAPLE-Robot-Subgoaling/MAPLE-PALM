package taxi.hierarchies.tasks.put;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.put.state.TaxiPutState;

public class TaxiPutTerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
		
	@Override
	public boolean isTerminal(State s) {
		TaxiPutState state = (TaxiPutState) s;
		
		for(String passengerName : state.getPassengers()){
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiPutDomain.ATT_IN_TAXI);
			if(!inTaxi) return false;
		}
		return true;
	}

}
