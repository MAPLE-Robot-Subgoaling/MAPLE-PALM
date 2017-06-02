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
		setRF(new GetRF());
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		GetAction action = (GetAction) a;
		String passname = action.passenger;
		TaxiL1State state = (TaxiL1State) s;
		TaxiL1Passenger pass = state.touchPassenger(passname);
		
		return pass.inTaxi || state.taxi.taxiOccupied;
	}
	
	public class GetRF implements RewardFunction{

		@Override
		public double reward(State s, Action a, State sprime) {
			GetAction action = (GetAction) a;
			String passname = action.passenger;
			TaxiL1State state = (TaxiL1State) s;
			TaxiL1Passenger pass = state.touchPassenger(passname);
			
			if(pass.inTaxi)
				return 1;
			else
				return 0;
		}
		
	}
}
