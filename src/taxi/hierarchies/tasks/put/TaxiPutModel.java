package taxi.hierarchies.tasks.put;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.put.DropoffActionType.DropoffAction;
import taxi.hierarchies.tasks.nav.NavigateActionType.NavigateAction;
import taxi.hierarchies.tasks.put.state.TaxiPutAgent;
import taxi.hierarchies.tasks.put.state.TaxiPutPassenger;
import taxi.hierarchies.tasks.put.state.TaxiPutState;

import java.util.ArrayList;
import java.util.List;

public class TaxiPutModel implements FullStateModel {

	@Override
	public State sample(State s, Action a) {
		List<StateTransitionProb> stpList = this.stateTransitions(s,a);
        double roll = RandomFactory.getMapped(0).nextDouble();
        double curSum = 0.;
        for(int i = 0; i < stpList.size(); i++){
            curSum += stpList.get(i).p;
            if(roll < curSum){
                return stpList.get(i).s;
            }
        }
        throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>();
		TaxiPutState state = (TaxiPutState) s;

		if(a.actionName().startsWith(TaxiPutDomain.ACTION_DROPOFF)) {
			dropoff(state, (DropoffAction)a, tps);
		} else if(a.actionName().startsWith(TaxiPutDomain.ACTION_NAV)) {
			navigate(state, (NavigateAction) a, tps);
		}
		return tps;
	}

	/**
	 * put the requested passenger into the taxi
	 * @param s the current state
	 * @param a the get action type
	 * @param tps the list of transition probabilities
	 */
	public void dropoff(TaxiPutState s, DropoffAction a, List<StateTransitionProb> tps) {
		TaxiPutState ns = s.copy();
		String passenger = a.getPassenger();

        TaxiPutPassenger np = s.touchPassenger(passenger);
        np.set(TaxiPutDomain.ATT_IN_TAXI, false);

		tps.add(new StateTransitionProb(ns, 1));
	}

	/**
	 * put the requested passenger into the taxi
	 * @param s the current state
	 * @param a the get action type
	 * @param tps the list of transition probabilities
	 */
	public void navigate(TaxiPutState s, NavigateAction a, List<StateTransitionProb> tps){
		TaxiPutState ns = s.copy();
		String goal = a.getGoalLocation();

		TaxiPutAgent nt = s.touchTaxi();
		nt.set(TaxiPutDomain.ATT_TAXI_LOCATION, goal);

		tps.add(new StateTransitionProb(ns, 1));
	}
}
