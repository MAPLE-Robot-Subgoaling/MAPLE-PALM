package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.DropoffActionType;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;

public class DropoffCompletedPF extends PropositionalFunction {
	//dropoff is complete when there is no passenger in the taxi
	
	public DropoffCompletedPF() {
		super("dropoff", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		TaxiDropoffState st = (TaxiDropoffState)s;
		DropoffActionType pickup = new DropoffActionType();
		DropoffActionType.DropoffAction a = pickup.associatedAction(action);
		String passenger = a.getPassenger();
		String pass_loc = (String)st.getPassengerAtt(passenger, TaxiDropoffDomain.ATT_LOCATION);

		return pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI);
	}

}
