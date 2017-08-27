package taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.state.TaxiState;

public class TaxiTerminalFunction implements TerminalFunction{
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
	
	@Override
	public boolean isTerminal(State s) {
		TaxiState state = (TaxiState) s;
		
		for(String passengerName : state.getPassengers()){
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
			if(inTaxi)
				return false;
			
			String passengerGoal = (String) state.getPassengerAtt(passengerName, Taxi.ATT_GOAL_LOCATION);
			int px = (int) state.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) state.getPassengerAtt(passengerName, Taxi.ATT_Y);
			
			for(String locName : state.getLocations()){
				if(passengerGoal.equals(locName)){
					int lx = (int) state.getLocationAtt(locName, Taxi.ATT_X);
					int ly = (int) state.getLocationAtt(locName, Taxi.ATT_Y);
					if(lx != px || ly != py)
						return false;
					
					break;
				}
			}
		}
		
		return true;
	}
}
