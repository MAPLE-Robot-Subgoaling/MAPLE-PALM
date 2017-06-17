package taxi.abstraction2;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.abstraction2.state.TaxiL2State;

public class GetActionType implements ActionType {

	@Override
	public String typeName() {
		return TaxiL2.ACTION_GET;
	}

	@Override
	public Action associatedAction(String strRep) {
		String passenger = strRep.split("_")[1];
		return new GetAction(passenger);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiL2State state = (TaxiL2State) s;
		List<Action> acts = new ArrayList<Action>();
		
		for(String passengerName : state.getPassengers()){
			acts.add(new GetAction(passengerName));
		}
		
		return acts;
	}

	public class GetAction implements Action{

		private String passenger;
		
		public GetAction(String passengerName) {
			this.passenger = passengerName;
		}
		
		public String getPassenger(){
			return passenger;
		}
		
		@Override
		public String actionName() {
			return TaxiL2.ACTION_GET + "_" + passenger;
		}

		@Override
		public Action copy() {
			return new GetAction(passenger);
		}
		
		@Override
		public String toString(){
			return actionName();
		}
	}
}
