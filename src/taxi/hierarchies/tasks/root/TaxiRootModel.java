package taxi.hierarchies.tasks.root;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.root.state.TaxiRootPassenger;
import taxi.hierarchies.tasks.root.state.TaxiRootState;
import utilities.MutableObject;

import java.util.ArrayList;
import java.util.List;

public class TaxiRootModel implements FullStateModel {

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
		TaxiRootState state = (TaxiRootState) s;

		if(a.actionName().startsWith(TaxiRootDomain.ACTION_GET)) {
			get(state, (ObjectParameterizedAction) a, tps);
		} else if(a.actionName().startsWith(TaxiRootDomain.ACTION_PUT)) {
			put(state, (ObjectParameterizedAction) a, tps);
		}
		return tps;
	}

	/**
	 * get the requested passenger into the taxi
	 * @param s the current state
	 * @param tps the list of transition probabilities
	 */
	public void get(TaxiRootState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
		TaxiRootState ns = s.copy();
		String passengerName = a.getObjectParameters()[0];
		TaxiRootPassenger np = ns.touchPassenger(passengerName);
		np.set(TaxiRootDomain.ATT_CURRENT_LOCATION, TaxiRootDomain.IN_TAXI);
		tps.add(new StateTransitionProb(ns, 1));
	}

	/**
	 * put the requested passenger into the taxi
	 * @param s the current state
	 * @param tps the list of transition probabilities
	 */
	public void put(TaxiRootState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
		TaxiRootState ns = s.copy();
		String passengerName = a.getObjectParameters()[0];
//		MutableObject passenger = (MutableObject) s.object(passengerName);
		TaxiRootPassenger np = ns.touchPassenger(passengerName);
		np.set(TaxiRootDomain.ATT_CURRENT_LOCATION, TaxiRootDomain.ATT_GOAL_LOCATION);
		tps.add(new StateTransitionProb(ns, 1));
	}
}
