package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.PutActionType;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.TaxiPutState;

public class PutFailurePF extends PropositionalFunction{
	//put fail if taxi is empty 
	
	public PutFailurePF() {
		super("put", new String[]{TaxiPutDomain.CLASS_LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PutActionType actyp = new PutActionType();
		PutActionType.PutAction a = actyp.associatedAction(action);
		TaxiPutState st = (TaxiPutState) s;

		boolean inTaxi = (boolean) st.getPassengerAtt(a.getPassengerName(), TaxiPutDomain.ATT_IN_TAXI);

		return !inTaxi;
	}
	
}
