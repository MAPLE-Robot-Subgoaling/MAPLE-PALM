package taxi.abstraction1;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.abstraction1.state.TaxiL1State;

import java.util.List;

public class TaxiL1TerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
	
	@Override
	public boolean isTerminal(State s) {
		TaxiL1State state = (TaxiL1State) s;
		
		for(String passengerName : state.getPassengers()){
			String location = (String) state.getPassengerAtt(passengerName, TaxiL1.ATT_CURRENT_LOCATION);
			//terminal by color
			String goalLocationColor = (String) state.getPassengerAtt(passengerName, TaxiL1.ATT_GOAL_LOCATION);
			boolean rightLocation=false;
			for(String color : (List<String>)state.getLocationAtt(location, Taxi.ATT_COLOR))
				if (color.equals(goalLocationColor))
					rightLocation=true;
			if(!rightLocation)
				return false;
			//end terminal by color
			
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
			boolean pickedUp = (boolean) state.getPassengerAtt(passengerName, TaxiL1.ATT_PICKED_UP_AT_LEAST_ONCE);
			if(inTaxi || !pickedUp)
				return false;
		}
		return true;
	}

}
