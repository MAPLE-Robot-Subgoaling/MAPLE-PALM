package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction1.state.TaxiL1State;
import taxi.abstraction2.PutActionType;
import taxi.abstraction2.TaxiL2;

public class PutFailurePF extends PropositionalFunction{
	//put fail if taxi is empty 
	
	public PutFailurePF() {
		super("put", new String[]{TaxiL2.CLASS_L2LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PutActionType actyp = new PutActionType();
		PutActionType.PutAction a = actyp.associatedAction(action);
		TaxiL1State st = (TaxiL1State) s;

		boolean inTaxi = (boolean) st.getPassengerAtt(a.getPassengerName(), TaxiL2.ATT_IN_TAXI);

		return !inTaxi;
	}
	
}
