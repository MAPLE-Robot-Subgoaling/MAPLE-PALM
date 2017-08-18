package taxi.hierarchies.tasks.bringon;

import java.util.ArrayList;
import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.PickupActionType.PickupAction;
import taxi.Taxi;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonPassenger;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;

public class TaxiBringonModel implements FullStateModel {

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
		TaxiBringonState state = (TaxiBringonState) s;
		
		if(a.actionName().startsWith(Taxi.ACTION_PICKUP)){
            pickup(state, (PickupAction)a, tps);
		}
		return tps;
	}


	/**
	 * put passenger in taxi if it is at the taxi
	 * @param s the current state
	 * @param tps the list of outcomes to add to
	 */
	public void pickup(TaxiBringonState s, PickupAction a, List<StateTransitionProb> tps){
		String taxiLocation = (String) s.getTaxiAtt(TaxiBringonDomain.ATT_LOCATION);
		String passenger = a.getPassenger();
		String passengerLocation = (String) s.getPassengerAtt(passenger, TaxiBringonDomain.ATT_LOCATION);
		TaxiBringonState ns = s.copy();

		//if no one is in taxi and it is at depot
		if(taxiLocation.equals(passengerLocation)) {
			TaxiBringonPassenger np = ns.touchPassenger(passenger);
			np.set(TaxiBringonDomain.ATT_LOCATION, TaxiBringonDomain.IN_TAXI);
		}
		tps.add(new StateTransitionProb(ns, 1));
	}
}
