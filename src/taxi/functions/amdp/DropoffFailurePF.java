package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;
import utilities.MutableObject;

public class DropoffFailurePF extends PropositionalFunction {
	//dropoff fails if taxi is not at a depot  
	
	public DropoffFailurePF() {
		super("dropoffFail", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		if (!(s instanceof TaxiDropoffState)) { return false; }
		MutableObject passenger = (MutableObject)s.object(params[0]);
		if (passenger == null) { return false; }
		String pass_loc = (String)passenger.get(TaxiDropoffDomain.ATT_LOCATION);

		return pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI) || pass_loc.equals(TaxiDropoffDomain.ON_ROAD);
	}
}
