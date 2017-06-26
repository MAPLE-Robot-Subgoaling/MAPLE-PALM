package taxi.rmaxq.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction2.GetActionType;
import taxi.abstraction2.GetActionType.GetAction;
import taxi.state.TaxiState;

public class BaseGetCompletedPF extends PropositionalFunction{ 
	//get is complete if desired passenger is in taxi  - no abstraction
	
	public BaseGetCompletedPF() {
		super("get", new String[]{TaxiL1.CLASS_L1PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		GetActionType actyp = new GetActionType();
		GetAction a = actyp.associatedAction(action);
		TaxiState st = (TaxiState) s;
		
		return (boolean) st.getPassengerAtt(a.getPassenger(), Taxi.ATT_IN_TAXI);
	}
}
