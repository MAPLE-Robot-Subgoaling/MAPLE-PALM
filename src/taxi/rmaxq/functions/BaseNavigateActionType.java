package taxi.rmaxq.functions;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.abstraction1.TaxiL1;
import taxi.state.TaxiState;

public class BaseNavigateActionType implements ActionType {
	//nav the taxi to the given goal  - no abstraction
	
	public String typeName() {
		return TaxiL1.ACTION_NAVIGATE;
	}

	@Override
	public NavigeteAction associatedAction(String strRep) {
		String goal = strRep.split("_")[1];
		return new NavigeteAction(goal);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiState state = (TaxiState) s;
		List<Action> acts = new ArrayList<Action>();
		
		for(String loc : state.getLocations()){
			acts.add(new NavigeteAction(loc));
		}
		
		return acts;
	}

	public class NavigeteAction implements Action {

		private String goalLocation;
		
		public NavigeteAction(String goal) {
			this.goalLocation = goal;
		}
		
		public String getGoalLocation(){
			return goalLocation;
		}
		
		@Override
		public String actionName() {
			return TaxiL1.ACTION_NAVIGATE + "_" + goalLocation;
		}

		@Override
		public Action copy() {
			return new NavigeteAction(goalLocation);
		}
		
		@Override
		public String toString(){
			return actionName();
		}
		
		@Override
		public boolean equals(Object other){
			if(this == other) return true;
			if(other == null || getClass() != other.getClass()) return false;
			
			NavigeteAction a = (NavigeteAction) other;
			
			return a.goalLocation.equals(goalLocation);
		}
		
		@Override
		public int hashCode(){
			return actionName().hashCode();
		}
	}
}
