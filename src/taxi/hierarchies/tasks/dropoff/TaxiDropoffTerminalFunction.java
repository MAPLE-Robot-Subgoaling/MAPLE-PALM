package taxi.hierarchies.tasks.dropoff;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;

public class TaxiDropoffTerminalFunction implements TerminalFunction {
	@Override
	public boolean isTerminal(State s) {
		TaxiDropoffState state = (TaxiDropoffState) s;
        return !((boolean)state.getTaxiAtt(TaxiDropoffDomain.ATT_TAXI_OCCUPIED));
	}

}
