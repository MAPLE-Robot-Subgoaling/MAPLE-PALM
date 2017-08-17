package taxi.hierarchies.tasks.get;

import java.util.ArrayList;
import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.get.GetActionType.GetAction;
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
		List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>();
		TaxiGetState state = (TaxiGetState) s;
	
		if(a.actionName().startsWith(TaxiGetDomain.ACTION_GET)){
			get(state, (GetAction) a, tps);
		}
		return tps;
	}

	/**
	 * put the requested passenger into the taxi
	 * @param s the current state
	 * @param a the get action type
	 * @param tps the list of transition probabilities
	 */
	public void get(TaxiGetState s, GetAction a, List<StateTransitionProb> tps){
	    String passegerName = a.getPassenger();
		TaxiGetState ns = s.copy();

		TaxiGetPassenger np = ns.touchPassenger(passegerName);
		np.set(TaxiGetDomain.ATT_IN_TAXI, true);
		tps.add(new StateTransitionProb(ns, 1));
	}
}
