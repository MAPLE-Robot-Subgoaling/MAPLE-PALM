package taxi.functions.rmaxq;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import taxi.state.TaxiState;

public class BaseGetActionType implements ActionType {
	//get action puts the requested passenger into taxi - no abstraction
	
	@Override
	public String typeName() {
		return TaxiRootDomain.ACTION_GET;
	}

	@Override
	public GetAction associatedAction(String strRep) {
		String passenger = strRep.split("_")[1];
		return new GetAction(passenger);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiState state = (TaxiState) s;
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
			return TaxiRootDomain.ACTION_GET + "_" + passenger;
		}

		@Override
		public Action copy() {
			return new GetAction(passenger);
		}
		
		@Override
		public String toString(){
			return actionName();
		}

		@Override
		public boolean equals(Object other){
			if(this == other) return true;
			if(other == null || getClass() != other.getClass()) return false;
			
			GetAction a = (GetAction) other;
			
			return a.passenger.equals(passenger);
		}
		
		@Override
		public int hashCode(){
			return actionName().hashCode();
		}
	}
}
