package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.get.GetActionType;
import taxi.hierarchies.tasks.get.GetActionType.GetAction;
import taxi.hierarchies.tasks.get.state.TaxiGetState;

public class GetCompletedPF extends PropositionalFunction{ 
	// get is complete when desired passenger is in the taxi
	
	public GetCompletedPF() {
		super("get", new String[]{TaxiGetDomain.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		TaxiGetState st = (TaxiGetState) s;
		GetActionType actyp = new GetActionType();
		GetAction a = actyp.associatedAction(action);
		String passenger = a.getPassenger();
		String pass_loc = (String)st.getPassengerAtt(passenger, TaxiGetDomain.ATT_LOCATION);

		return pass_loc.equals(TaxiGetDomain.IN_TAXI);
	}
}
