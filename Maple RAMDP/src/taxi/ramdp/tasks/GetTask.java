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
import taxi.amdp.level2.TaxiL2Domain.GetType.GetAction;

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
		TaxiL1State state = (TaxiL1State) s;		
//		return state.taxi.taxiOccupied;
		for(TaxiL1Passenger p : state.passengers){
			if(p.inTaxi){
				return true;
			}
		}
		return false;
	}
	
	public boolean isComplete(State s, Action a){
		GetAction action = (GetAction) a;
		String passname = action.passenger;
		TaxiL1State state = (TaxiL1State) s;
		TaxiL1Passenger pass = state.touchPassenger(passname);
		
		return pass.inTaxi;
	}
}
