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
			String pLocation = (String) state.getPassengerAtt(passengerName, TaxiPutDomain.ATT_LOCATION);
            String pGoal = (String) state.getPassengerAtt(passengerName, TaxiPutDomain.ATT_GOAL_LOCATION);
            // if there is a passenger not at its goal, then it is false
            if (!pLocation.equals(pGoal)) {
                return false;
            }
		}
		return true;
	}

}
