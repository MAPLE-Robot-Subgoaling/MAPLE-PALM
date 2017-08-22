package taxi.hierarchies.tasks.put;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.put.state.TaxiPutState;

public class NavigateActionType implements ActionType {
	//the navigate actions are for moving the taxi to each of the depots
	
	public String typeName() {
		return TaxiPutDomain.ACTION_NAV;
	}

	@Override
	public NavigateAction associatedAction(String strRep) {
		String goal = strRep.split("_")[1];
		return new NavigateAction(goal);
	}

	//there is a action for each depot in the current configuration
	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiPutState state = (TaxiPutState) s;
		List<Action> acts = new ArrayList<Action>();
		
		for(String loc : state.getLocations()){
			acts.add(new NavigateAction(loc));
		}
		
		return acts;
	}

	//each navigate action is given a goal
	public class NavigateAction implements Action {

		private String goalLocation;
		
		public NavigateAction(String goal) {
			this.goalLocation = goal;
		}
		
		public String getGoalLocation(){
			return goalLocation;
		}
		
		@Override
		public String actionName() {
			return TaxiPutDomain.ACTION_NAV + "_" + goalLocation;
		}

		@Override
		public Action copy() {
			return new NavigateAction(goalLocation);
		}
		
		@Override
		public String toString(){
			return actionName();
		}
		
		@Override
		public boolean equals(Object other){
			if(this == other) return true;
			if(other == null || getClass() != other.getClass()) return false;
			
			NavigateAction a = (NavigateAction) other;
			
			return a.goalLocation.equals(goalLocation);
		}
		
		@Override
		public int hashCode(){
			return actionName().hashCode();
		}
	}
}
