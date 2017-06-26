package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction1.state.TaxiL1State;
import taxi.abstraction2.PutActionType;
import taxi.abstraction2.PutActionType.PutAction;

public class PutCompletedPF extends PropositionalFunction{
	//put is complete when the passenger at the put actions goal 
	
	public PutCompletedPF() {
		super("put", new String[]{TaxiL1.CLASS_L1LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PutActionType actyp = new PutActionType();
		PutAction a = actyp.associatedAction(action);
		TaxiL1State st = (TaxiL1State) s;
		boolean taxiOccupied = (boolean) st.getTaxiAtt(TaxiL1.ATT_TAXI_OCCUPIED);
		
		//is the passenger that is at goal not in the taxi
		for(String passengerName : st.getPassengers()){
			String pLocation = (String) st.getPassengerAtt(passengerName, TaxiL1.ATT_CURRENT_LOCATION);
			if(pLocation.equals(a.getGoalLocation())){
				return !((boolean) st.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI)) && ! taxiOccupied;
			}
		}
		
		return false;
	}
}
