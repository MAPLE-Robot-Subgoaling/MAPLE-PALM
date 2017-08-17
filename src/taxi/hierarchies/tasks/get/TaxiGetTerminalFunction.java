package taxi.hierarchies.tasks.get;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.get.state.TaxiGetState;

public class TaxiGetTerminalFunction implements TerminalFunction {
	@Override
	public boolean isTerminal(State s) {
		TaxiGetState state = (TaxiGetState) s;
		
		for(String passengerName : state.getPassengers()){
			boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, TaxiGetDomain.ATT_IN_TAXI);
			if(!inTaxi) return false;
		}
		return true;
	}

}
