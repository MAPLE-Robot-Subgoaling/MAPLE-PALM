package taxi.hierarchies.tasks.root;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.root.state.TaxiRootState;

public class PutActionType implements ActionType {
	//the put action which put the current ride at the given location
	@Override
	public String typeName() {
		return TaxiRootDomain.ACTION_PUT;
	}

	@Override
	public PutAction associatedAction(String strRep) {
	    String[] parameters = strRep.split("_");
		return new PutAction(parameters[1]);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiRootState state = (TaxiRootState) s;
		List<Action> acts = new ArrayList<>();

		for(String pass : state.getPassengers()) {
		    String loc = (String)state.getPassengerAtt(pass, TaxiRootDomain.ATT_CURRENT_LOCATION);
			if(loc.equals(TaxiRootDomain.IN_TAXI)) {
                acts.add(new PutAction( pass));
			}
		}

		return acts;
	}

	public class PutAction implements Action{

		private String passengerName;
		
		public PutAction(String passenger) {
			this.passengerName = passenger;
		}
		
		public String getPassenger() {
			return passengerName;
		}
		
		@Override
		public String actionName() {
			return TaxiRootDomain.ACTION_PUT + "_" + passengerName;
		}

		@Override
		public Action copy() {
			return new PutAction(passengerName);
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
			
			return a.passengerName.equals(passengerName);
		}
		
		@Override
		public int hashCode(){
			return actionName().hashCode();
		}
	}
}
