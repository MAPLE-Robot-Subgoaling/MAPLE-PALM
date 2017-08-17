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
		GetActionType actyp = new GetActionType();
		GetAction a = actyp.associatedAction(action);
		TaxiGetState st = (TaxiGetState) s;
		
		return (boolean) st.getPassengerAtt(a.getPassenger(), TaxiGetDomain.ATT_IN_TAXI);
	}
}
