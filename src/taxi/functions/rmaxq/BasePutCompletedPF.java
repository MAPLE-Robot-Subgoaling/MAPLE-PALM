package taxi.functions.rmaxq;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BasePutCompletedPF extends PropositionalFunction{
	//put is complete when the passenenger is at goal and taxi is empty - no abstraction
	
	public BasePutCompletedPF() {
		super("put", new String[]{Taxi.CLASS_LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		BasePutActionType actyp = new BasePutActionType();
		BasePutActionType.PutAction a = actyp.associatedAction(action);
		TaxiState st = (TaxiState) s;

		String passenger = a.getPassenger();
		boolean inTaxi = (boolean)st.getPassengerAtt(passenger, Taxi.ATT_IN_TAXI);
		String goalLocation = (String)st.getPassengerAtt(passenger, Taxi.ATT_GOAL_LOCATION);

		//is the passenger that is at goal not in the taxi
		for(String locName : st.getLocations()){
		    if(locName.equals(goalLocation)) {
		    	return !inTaxi;
			}
		}
		
		return false;
	}
}
