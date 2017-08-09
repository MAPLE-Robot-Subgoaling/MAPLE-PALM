package taxi.abstraction2;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction2.state.TaxiL2State;

public class PutActionType implements ActionType {
	//the put action which put the current ride at the given location
	@Override
	public String typeName() {
		return TaxiL2.ACTION_PUT;
	}

	@Override
	public PutAction associatedAction(String strRep) {
		String[] parameters = strRep.split("_");
		return new PutAction(parameters[1], parameters[2]);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiL2State state = (TaxiL2State) s;
		List<Action> acts = new ArrayList<>();

		for (String pass : state.getPassengers()) {
			if ((boolean)state.getPassengerAtt(pass, TaxiL2.ATT_IN_TAXI)) {
				for(String loc : state.getLocations()){
					acts.add(new PutAction(loc, pass));
				}
			}
		}
		
		return acts;
	}

	public class PutAction implements Action{

		private String goalLocation;
		private String passengerName;
		
		public PutAction(String goal, String passenger) {
			this.goalLocation = goal;
			this.passengerName = passenger;
		}
		
		public String getGoalLocation(){
			return goalLocation;
		}

		public String getPassengerName() { return passengerName; }
		
		@Override
		public String actionName() {
			return TaxiL2.ACTION_PUT + "_" + goalLocation + "_" + passengerName;
		}

		@Override
		public Action copy() {
			return new PutAction(goalLocation, passengerName);
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
			
			return a.goalLocation.equals(goalLocation) && a.passengerName.equals(passengerName);
		}
		
		@Override
		public int hashCode(){
			return actionName().hashCode();
		}
	}
}
