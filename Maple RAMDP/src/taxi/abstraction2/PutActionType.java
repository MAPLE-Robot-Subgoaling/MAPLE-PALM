package taxi.abstraction2;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.abstraction1.NavigateActionType.NavigeteAction;
import taxi.abstraction2.state.TaxiL2State;

public class PutActionType implements ActionType {

	@Override
	public String typeName() {
		return TaxiL2.ACTION_PUT;
	}

	@Override
	public PutAction associatedAction(String strRep) {
		String goal = strRep.split("_")[1];
		return new PutAction(goal);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiL2State state = (TaxiL2State) s;
		List<Action> acts = new ArrayList<>();
		
		for(String loc : state.getLocations()){
			acts.add(new PutAction(loc));
		}
		
		return acts;
	}

	public class PutAction implements Action{

		private String goalLocation;
		
		public PutAction(String goal) {
			this.goalLocation = goal;
		}
		
		public String getGoalLocation(){
			return goalLocation;
		}
		
		@Override
		public String actionName() {
			return TaxiL2.ACTION_PUT + "_" + goalLocation;
		}

		@Override
		public Action copy() {
			return new PutAction(goalLocation);
		}
		
		@Override
		public String toString(){
			return actionName();
		}
		

		@Override
		public boolean equals(Object other){
			if(this == other) return true;
			if(other == null || getClass() != other.getClass()) return false;
			
			PutAction a = (PutAction) other;
			
			return a.goalLocation.equals(goalLocation);
		}
		
		@Override
		public int hashCode(){
			return actionName().hashCode();
		}
	}
}
