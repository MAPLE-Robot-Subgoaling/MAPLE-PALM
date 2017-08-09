package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.DropOffActionType;
import taxi.Taxi;
import taxi.state.TaxiState;

public class DropoffCompletedPF extends PropositionalFunction {
	//dropoff is complete when there is no passenger in the taxi
	
	public DropoffCompletedPF() {
		super("dropoffL1", new String[]{Taxi.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		DropOffActionType pass = new DropOffActionType();
		DropOffActionType.DropOffAction a = pass.associatedAction(action);
		TaxiState st = (TaxiState) s;

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);

		int px = (int) st.getPassengerAtt(a.getPassenger(), Taxi.ATT_X);
		int py = (int) st.getPassengerAtt(a.getPassenger(), Taxi.ATT_Y);

		boolean inTaxi = (boolean) st.getPassengerAtt(a.getPassenger(), Taxi.ATT_IN_TAXI);


		if(tx == px && ty == py && !inTaxi){
			return true;
		}

		return false;
	}

}
