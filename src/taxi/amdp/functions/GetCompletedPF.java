package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction1.state.TaxiL1State;
import taxi.abstraction2.GetActionType;
import taxi.abstraction2.GetActionType.GetAction;

public class GetCompletedPF extends PropositionalFunction{ 

	public GetCompletedPF() {
		super("get", new String[]{TaxiL1.CLASS_L1PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		GetActionType actyp = new GetActionType();
		GetAction a = actyp.associatedAction(action);
		TaxiL1State st = (TaxiL1State) s;
		
		return (boolean) st.getPassengerAtt(a.getPassenger(), TaxiL1.ATT_IN_TAXI);
	}
}
