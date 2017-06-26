package taxi.rmaxq.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction2.PutActionType;
import taxi.abstraction2.PutActionType.PutAction;
import taxi.state.TaxiState;

public class BasePutCompletedPF extends PropositionalFunction{
	//put is complete when the passenenger is at goal and taxi is empty - no abstraction
	
	public BasePutCompletedPF() {
		super("put", new String[]{TaxiL1.CLASS_L1LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		PutActionType actyp = new PutActionType();
		PutAction a = actyp.associatedAction(action);
		TaxiState st = (TaxiState) s;
		
		int lx = (int) st.getLocationAtt(a.getGoalLocation(), Taxi.ATT_X);
		int ly = (int) st.getLocationAtt(a.getGoalLocation(), Taxi.ATT_Y);
		//is the passenger that is at goal not in the taxi
		for(String passengerName : st.getPassengers()){
			int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
			
			if(lx == px && ly == py){
				return !((boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI));
			}
		}
		
		return false;
	}
}
