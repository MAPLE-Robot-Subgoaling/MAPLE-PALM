package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.abstraction1.PickupActionType;
import taxi.state.TaxiState;

public class PickupFailurePF extends PropositionalFunction {
	//pickup fails when taxi is not at a depot
	public PickupFailurePF() {
		super("pickup>L!", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		// if the taxi is not at depot or not occupied
		String action = params[0];
		PickupActionType pass = new PickupActionType();
		PickupActionType.PickupAction a = pass.associatedAction(action);
		TaxiState st = (TaxiState) s;
		
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		
        int px = (int) st.getPassengerAtt(a.getPassenger(), Taxi.ATT_X);
        int py = (int) st.getPassengerAtt(a.getPassenger(), Taxi.ATT_Y);
        if(tx == px && ty == py){
            return (boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);
        }

		return true;
	}

}
