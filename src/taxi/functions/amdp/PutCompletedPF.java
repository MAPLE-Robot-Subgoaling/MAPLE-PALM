package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.TaxiPutState;
import taxi.hierarchies.tasks.put.PutActionType;
import taxi.hierarchies.tasks.put.PutActionType.PutAction;

public class PutCompletedPF extends PropositionalFunction{
	//put is complete when the passenger at the put actions goal 
	
	public PutCompletedPF() {
		super("put", new String[]{TaxiPutDomain.CLASS_LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PutActionType actyp = new PutActionType();
		PutAction a = actyp.associatedAction(action);
		TaxiPutState st = (TaxiPutState) s;

		//is the passenger that is at goal not in the taxi
		String pLocation = (String) st.getPassengerAtt(a.getPassengerName(), TaxiPutDomain.ATT_CURRENT_LOCATION);
		if(pLocation.equals(a.getGoalLocation())){
			return !((boolean) st.getPassengerAtt(a.getPassengerName(), TaxiPutDomain.ATT_IN_TAXI));
		}

return false;
	}
}
