package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.PickupActionType;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BringonCompletedPF extends PropositionalFunction {
	//pickup is complete when passenger is in taxi 
	
	public BringonCompletedPF() {
		super("bringon", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PickupActionType pass = new PickupActionType();
		PickupActionType.PickupAction a = pass.associatedAction(action);
		TaxiState st = (TaxiState) s;
		return (boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);
	}

}
