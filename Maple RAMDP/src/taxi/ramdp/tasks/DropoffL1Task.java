package taxi.ramdp.tasks;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.framework.NonprimitiveTask;
import ramdp.framework.Task;
import taxi.state.TaxiAgent;
import taxi.state.TaxiState;

public class DropoffL1Task extends NonprimitiveTask{

	/**
	 * create a dropoff task in level 1
	 * @param children the subtasks
	 * @param aType dropoff action
	 * @param abstractDomain abstract l1 domain
	 * @param map state mapper to level 1
	 */
	public DropoffL1Task(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map) {
		super(children, aType, abstractDomain, map);
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		TaxiState state = (TaxiState) s;
		TaxiAgent taxi = state.taxi;
		return !taxi.taxiOccupied;
	}

}
