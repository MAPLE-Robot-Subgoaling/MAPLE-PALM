package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import utilities.MutableObject;

public class DropoffCompletedPF extends PropositionalFunction {
	//dropoff is complete when there is no passenger in the taxi
	
	public DropoffCompletedPF() {
		super("dropoff", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		MutableObject passenger = (MutableObject)s.object(params[0]);
		if (passenger == null) { return false; }
		String pass_loc = (String)passenger.get(TaxiDropoffDomain.ATT_LOCATION);
		return pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI);
	}

}
