package taxi.ramdp.tasks;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.framework.NonprimitiveTask;
import ramdp.framework.Task;
import taxi.amdp.level2.TaxiL2Domain.GetType.GetAction;
import taxi.amdp.level2.state.TaxiL2Passenger;
import taxi.amdp.level2.state.TaxiL2State;

public class GetTask extends NonprimitiveTask {

	/**
	 * Creates a L2 get task
	 * @param children The subtasks
	 * @param aType The get action
	 * @param abstractDomain L2 taxi domain
	 * @param map state mapper to L2
	 */
	public GetTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map) {
		super(children, aType, abstractDomain, map);
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		GetAction action = (GetAction) a;
		String passname = action.passenger;
		TaxiL2State state = (TaxiL2State) s;
		TaxiL2Passenger pass = state.touchPassenger(passname);
		
		return pass.inTaxi;
	}
}
