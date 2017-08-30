package taxi;

import burlap.mdp.core.action.Action;

//each navigate action is given a goal
public class PutdownAction implements Action {

	private String passenger;

	public PutdownAction(String passenger) {
		this.passenger = passenger;
	}

	public String getPassenger(){
		return passenger;
	}

	@Override
	public String actionName() {
		return Taxi.ACTION_PUTDOWN + "_" + passenger;
	}

	@Override
	public Action copy() {
		return new PutdownAction(passenger);
	}

	@Override
	public String toString(){
		return actionName();
	}

	@Override
	public boolean equals(Object other){
		if(this == other) return true;
		if(other == null || getClass() != other.getClass()) return false;

		PutdownAction a = (PutdownAction) other;

		return a.passenger.equals(passenger);
	}

	@Override
	public int hashCode(){
		return actionName().hashCode();
	}
}

