package taxi.hierarchies.tasks.put;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.put.PutActionType.PutAction;
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
	
		if(a.actionName().startsWith(TaxiPutDomain.ACTION_PUT)){
			put(state, (PutAction) a , tps);
		}
		return tps;
	}

	/**
	 * puts current passenger at requested depot
	 * @param s the current state
	 * @param a the put action
	 * @param tps the list of state transition probabilities
	 */
	public void put(TaxiPutState s, PutAction a, List<StateTransitionProb> tps){
		String goalLoaction = a.getGoalLocation();
		TaxiPutState ns = s.copy();

		int passengersAtGoal = 0;
		for(String passengerNamer : s.getPassengers()){
			String pLocation = (String) s.getPassengerAtt(passengerNamer, TaxiPutDomain.ATT_CURRENT_LOCATION);
			boolean inTaxi = (boolean) s.getPassengerAtt(passengerNamer, TaxiPutDomain.ATT_IN_TAXI);
			if(goalLoaction.equals(pLocation) && !inTaxi)
				passengersAtGoal++;
		}

		for(String passengerName : s.getPassengers()){
			boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, TaxiPutDomain.ATT_IN_TAXI);
			if(inTaxi){
				//change location and remove from taxi

				if(passengersAtGoal == 0){
					TaxiPutPassenger np = ns.touchPassenger(passengerName);
					np.set(TaxiPutDomain.ATT_CURRENT_LOCATION, goalLoaction);
					np.set(TaxiPutDomain.ATT_IN_TAXI, false);
				}
			}
		}
		tps.add(new StateTransitionProb(ns, 1));
	}
}
