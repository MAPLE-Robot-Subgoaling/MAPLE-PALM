package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction1.state.TaxiL1State;
import taxi.abstraction2.PutActionType;
import taxi.abstraction2.PutActionType.PutAction;

public class PutCompletedPF extends PropositionalFunction{

	public PutCompletedPF() {
		super("put", new String[]{TaxiL1.CLASS_L1LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PutActionType actyp = new PutActionType();
		PutAction a = actyp.associatedAction(action);
		TaxiL1State st = (TaxiL1State) s;
		
		for(String pass : )
	}
}
