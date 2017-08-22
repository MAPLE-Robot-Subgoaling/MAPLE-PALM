package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.TaxiPutState;
import taxi.hierarchies.tasks.root.PutActionType;

public class PutCompletedPF extends PropositionalFunction{
	//put is complete when the passenger at the put actions goal 
	
	public PutCompletedPF() {
		super("put", new String[]{TaxiPutDomain.CLASS_PASSENGER});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		TaxiPutState st = (TaxiPutState) s;
		PutActionType actyp = new PutActionType();
		PutActionType.PutAction a = actyp.associatedAction(action);
		String passenger = a.getPassenger();
		String goal_loc = (String)st.getPassengerAtt(passenger, TaxiPutDomain.ATT_GOAL_LOCATION);
		boolean inTaxi = (boolean)st.getPassengerAtt(passenger, TaxiPutDomain.ATT_IN_TAXI);
		String taxi_loc = (String)st.getTaxiAtt(TaxiPutDomain.ATT_TAXI_LOCATION);

		return taxi_loc.equals(goal_loc) && !inTaxi;
	}
}
