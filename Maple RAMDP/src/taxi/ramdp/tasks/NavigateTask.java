package taxi.ramdp.tasks;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.framework.NonprimitiveTask;
import ramdp.framework.Task;
import taxi.amdp.level1.TaxiL1Domain.NavigateType.NavigateAction;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiState;

public class NavigateTask extends NonprimitiveTask {

	/**
	 * Create a L1 nav action
	 * @param children the subtasks
	 * @param aType Nav action type
	 * @param abstractDomain L1 abstract domain
	 * @param map state mapper to L1
	 */
	public NavigateTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map) {
		super(children, aType, abstractDomain, map);
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		TaxiState state = (TaxiState) s;
		NavigateAction action = (NavigateAction) a;
		String goalLocation = action.location;
		TaxiLocation goal = state.touchLocation(state.locationIndWithColour(goalLocation));
		TaxiAgent t = state.taxi;
		
		return t.x == goal.x && t.y == goal.y; 
	}
}
