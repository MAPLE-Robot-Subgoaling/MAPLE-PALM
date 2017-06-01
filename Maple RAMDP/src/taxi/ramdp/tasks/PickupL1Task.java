package taxi.ramdp.tasks;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.framework.NonprimitiveTask;
import ramdp.framework.Task;
import taxi.amdp.level1.state.TaxiL1Agent;
import taxi.amdp.level1.state.TaxiL1State;

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
		TaxiL1State state = (TaxiL1State) s;
		TaxiL1Agent taxi = state.taxi;
		return taxi.taxiOccupied;
	}

}
