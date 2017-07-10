package taxi.abstraction2;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.abstraction2.state.TaxiL2State;

import java.util.List;

public class TaxiL2TerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
		
	@Override
	public boolean isTerminal(State s) {
		TaxiL2State state = (TaxiL2State) s;
		
		for(String passengerName : state.getPassengers()){
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiL2.ATT_IN_TAXI);
			boolean pickedUp = (boolean) state.getPassengerAtt(passengerName, TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE);
			if(inTaxi || !pickedUp)
				return false;
			String location = (String) state.getPassengerAtt(passengerName, TaxiL2.ATT_CURRENT_LOCATION);
			String goalLocationColor = (String) state.getPassengerAtt(passengerName, TaxiL2.ATT_GOAL_LOCATION);
			//terminal by color
			boolean rightLocation=false;
			for(String color : (List<String>)state.getLocationAtt(location, Taxi.ATT_COLOR))
				if (color.equals(goalLocationColor))
					rightLocation=true;
			if(!rightLocation)
				return false;
			//end terminal by color
		}
		System.out.println("L2 is terminating!");
		return true;
	}

}
