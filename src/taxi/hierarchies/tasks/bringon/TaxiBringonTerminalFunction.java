package taxi.hierarchies.tasks.bringon;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;

public class TaxiBringonTerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
	
	@Override
	public boolean isTerminal(State s) {
		TaxiBringonState state = (TaxiBringonState) s;
		
		for(String passengerName : state.getPassengers()){
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiBringonDomain.ATT_IN_TAXI);
			if(!inTaxi)
				return false;
		}
		return true;
	}

}
