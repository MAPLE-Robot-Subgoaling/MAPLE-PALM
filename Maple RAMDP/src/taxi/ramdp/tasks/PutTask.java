package taxi.ramdp.tasks;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.framework.NonprimitiveTask;
import ramdp.framework.Task;
import taxi.amdp.level2.TaxiL2Domain.PutType.PutAction;
import taxi.amdp.level2.state.TaxiL2Passenger;
import taxi.amdp.level2.state.TaxiL2State;

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
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		PutAction action = (PutAction) a;
		TaxiL2State state = (TaxiL2State) s;
		String goalLocation = action.location;
		String passname = action.passenger;
		TaxiL2Passenger pass = state.touchPassenger(passname);
		
		return pass.currentLocation.equals(goalLocation);
	}

}
