package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.DropoffActionType;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;

public class DropoffFailurePF extends PropositionalFunction {
	//dropoff fails if taxi is not at a depot  
	
	public DropoffFailurePF() {
		super("dropoffFail", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		TaxiDropoffState st = (TaxiDropoffState)s;
		DropoffActionType pickup = new DropoffActionType();
		DropoffActionType.DropoffAction a = pickup.associatedAction(action);
		String passenger = a.getPassenger();
		String pass_loc = (String)st.getPassengerAtt(passenger, TaxiDropoffDomain.ATT_LOCATION);

		return pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI) || pass_loc.equals(TaxiDropoffDomain.ON_ROAD);
	}
}
