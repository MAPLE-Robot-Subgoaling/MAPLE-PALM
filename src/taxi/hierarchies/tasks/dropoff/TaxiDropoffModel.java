package taxi.hierarchies.tasks.dropoff;

import java.util.ArrayList;
import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.Taxi;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffPassenger;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;

public class TaxiDropoffModel implements FullStateModel {

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
		TaxiDropoffState state = (TaxiDropoffState) s;
		
		if(a.actionName().startsWith(Taxi.ACTION_PUTDOWN)){
			putdown(state, (ObjectParameterizedAction)a, tps);
		}
		return tps;
	}

	/**
	 * put the passenger that is in the taxi on a depot 
	 * @param s
	 * @param tps
	 */
	public void putdown(TaxiDropoffState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
		String passenger = (String)a.getObjectParameters()[0];
		TaxiDropoffState ns = s.copy();

		String pass_loc = (String)s.getPassengerAtt(passenger, TaxiDropoffDomain.ATT_LOCATION);

		//if some one is in taxi and it is at depot
		if(!(pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI) || pass_loc.equals(TaxiDropoffDomain.ON_ROAD))){
			TaxiDropoffPassenger np = ns.touchPassenger(passenger);
			np.set(TaxiDropoffDomain.ATT_LOCATION, TaxiDropoffDomain.NOT_IN_TAXI);
		}
		tps.add(new StateTransitionProb(ns, 1));
	}
}
