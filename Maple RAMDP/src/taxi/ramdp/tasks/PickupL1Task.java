package taxi.ramdp.tasks;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.Task;
import taxi.state.TaxiAgent;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

public class PickupL1Task extends NonprimitiveTask {

	/**
	 * Creates a pickup task
	 * @param children subtasks
	 * @param aType pickup action type
	 * @param abstractDomain L1 abstract domain
	 * @param map state mapper to L1
	 */
	public PickupL1Task(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map) {
		super(children, aType, abstractDomain, map);
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		TaxiState state = (TaxiState) s;
		TaxiAgent taxi = state.taxi;
	
		//if there is no passenger its terminal 
		for(TaxiPassenger p : state.passengers){
			if(p.x == taxi.x && p.y == taxi.y)
				return taxi.taxiOccupied;
		}
		return true;
	}
}
