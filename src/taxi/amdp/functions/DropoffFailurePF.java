package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.DropOffActionType;
import taxi.Taxi;
import taxi.state.TaxiState;

public class DropoffFailurePF extends PropositionalFunction {
	//dropoff fails if taxi is not at a depot  
	
	public DropoffFailurePF() {
		super("dropoff>L!", new String[]{Taxi.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		// if the taxi is not at depot or not occupied
		String action = params[0];
		DropOffActionType pass = new DropOffActionType();
		DropOffActionType.DropOffAction a = pass.associatedAction(action);
		TaxiState st = (TaxiState) s;

		return !(boolean)st.getPassengerAtt(a.getPassenger(), Taxi.ATT_IN_TAXI);

	}
}
