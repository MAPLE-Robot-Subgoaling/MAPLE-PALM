package taxi.ramdp.tasks;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.Task;
import taxi.amdp.level1.state.TaxiL1Passenger;
import taxi.amdp.level1.state.TaxiL1State;
import taxi.amdp.level2.TaxiL2Domain.PutType.PutAction;

public class PutTask extends NonprimitiveTask{

	/**
	 * Creates a put task
	 * @param children 
	 * @param aType
	 * @param abstractDomain
	 * @param map
	 */
	public PutTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map) {
		super(children, aType, abstractDomain, map);
		this.setRF(new PutRF());
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		PutAction action = (PutAction) a;
		TaxiL1State state = (TaxiL1State) s;
		String goalLocation = action.location;
		String passname = action.passenger;
		TaxiL1Passenger pass = state.touchPassenger(passname);
		if(!state.taxi.taxiOccupied)
			return true;
		return pass.currentLocation.equals(goalLocation) && pass.pickUpOnce;
	}

	public class PutRF implements RewardFunction{

		@Override
		public double reward(State s, Action a, State sprime) {
			PutAction action = (PutAction) a;
			TaxiL1State state = (TaxiL1State) s;
			String goalLocation = action.location;
			String passname = action.passenger;
			TaxiL1Passenger pass = state.touchPassenger(passname);

			if(pass.currentLocation.equals(goalLocation) && !pass.inTaxi && pass.pickUpOnce)
				return 1;
			else
				return 0;
		}
		
	}
}
