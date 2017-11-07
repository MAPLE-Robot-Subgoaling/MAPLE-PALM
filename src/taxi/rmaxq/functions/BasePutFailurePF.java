package taxi.rmaxq.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.Taxi;
import taxi.abstraction1.state.TaxiL1State;
import taxi.abstraction2.PutActionType;
import taxi.state.TaxiState;

public class BasePutFailurePF extends PropositionalFunction{
	//put fail when taxi is empty - no abstraction
	
	public BasePutFailurePF() {
		super("put", new String[]{TaxiL1.CLASS_L1LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PutActionType actyp = new PutActionType();
		PutActionType.PutAction a = actyp.associatedAction(action);
		TaxiState st = (TaxiState) s;

		boolean inTaxi = (boolean) st.getPassengerAtt(a.getPassengerName(), TaxiL1.ATT_IN_TAXI);
		return !inTaxi;
	}
}
