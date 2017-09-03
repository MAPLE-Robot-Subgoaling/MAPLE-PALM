package taxi.hierarchies.tasks.get;

import java.util.ArrayList;
import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.get.state.TaxiGetAgent;
import taxi.hierarchies.tasks.get.state.TaxiGetPassenger;
import taxi.hierarchies.tasks.get.state.TaxiGetState;

public class TaxiGetModel implements FullStateModel {

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
		List<StateTransitionProb> tps = new ArrayList<>();
		TaxiGetState state = (TaxiGetState) s;
	
		if(a.actionName().startsWith(TaxiGetDomain.ACTION_BRINGON)) {
			bringon(state, (ObjectParameterizedAction) a, tps);
		} else if(a.actionName().startsWith(TaxiGetDomain.ACTION_NAV)) {
			navigate(state, (ObjectParameterizedAction)a, tps);
		}
		return tps;
	}

	/**
	 * put the requested passenger into the taxi
	 * @param s the current state
	 * @param a the get action type
	 * @param tps the list of transition probabilities
	 */
	public void bringon(TaxiGetState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
		TaxiGetState ns = s.copy();
		String passenger = a.getObjectParameters()[0];

		TaxiGetPassenger np = s.touchPassenger(passenger);
		np.set(TaxiGetDomain.ATT_LOCATION, TaxiGetDomain.IN_TAXI);

		tps.add(new StateTransitionProb(ns, 1));
	}

	/**
	 * put the requested passenger into the taxi
	 * @param s the current state
	 * @param a the get action type
	 * @param tps the list of transition probabilities
	 */
	public void navigate(TaxiGetState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
		TaxiGetState ns = s.copy();
		String goal = a.getObjectParameters()[0];

		TaxiGetAgent nt = s.touchTaxi();
		nt.set(TaxiGetDomain.ATT_LOCATION, goal);

		tps.add(new StateTransitionProb(ns, 1));
	}
}
