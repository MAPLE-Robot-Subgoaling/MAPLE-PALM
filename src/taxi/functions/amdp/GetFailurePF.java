package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.get.GetActionType;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.get.state.TaxiGetState;

public class GetFailurePF extends PropositionalFunction {
	//get fails if any passenger if in taxi unless it is the right one
	
	public GetFailurePF() {
		super("getFail", new String[]{TaxiGetDomain.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		TaxiGetState st = (TaxiGetState) s;
		GetActionType actyp = new GetActionType();
		GetActionType.GetAction a = actyp.associatedAction(action);
		String passenger = a.getPassenger();
		String pass_loc = (String)st.getPassengerAtt(passenger, TaxiGetDomain.ATT_LOCATION);
		String taxi_loc = (String)st.getTaxiAtt(TaxiGetDomain.ATT_LOCATION);

		return !pass_loc.equals(taxi_loc);
	}

}
