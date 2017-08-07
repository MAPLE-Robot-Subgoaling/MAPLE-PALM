package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.PickupActionType;
import taxi.Taxi;
import taxi.state.TaxiState;

public class PickupCompletedPF extends PropositionalFunction {
	//pickup is complete when passenger is in taxi 
	
	public PickupCompletedPF() {
		super("pickupL1", new String[]{Taxi.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PickupActionType pass = new PickupActionType();
		PickupActionType.PickupAction a = pass.associatedAction(action);
		TaxiState st = (TaxiState) s;

		return (boolean)st.getPassengerAtt(a.getPassenger(), Taxi.ATT_IN_TAXI);
	}

}
