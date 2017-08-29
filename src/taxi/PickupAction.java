package taxi.hierarchies.tasks.bringon;

import burlap.mdp.core.action.Action;
import taxi.Taxi;

public class PickupAction implements Action {

	private String passenger;

	public PickupAction(String passenger) {
		this.passenger = passenger;
	}

	public String getPassenger(){
		return passenger;
	}

	@Override
	public String actionName() {
		return Taxi.ACTION_PICKUP + "_" + passenger;
	}

	@Override
	public Action copy() {
		return new PickupAction(passenger);
	}

	@Override
	public String toString(){
		return actionName();
	}

	@Override
	public boolean equals(Object other){
		if(this == other) return true;
		if(other == null || getClass() != other.getClass()) return false;

		PickupAction a = (PickupAction) other;

		return a.passenger.equals(passenger);
	}

	@Override
	public int hashCode(){
		return actionName().hashCode();
	}
}