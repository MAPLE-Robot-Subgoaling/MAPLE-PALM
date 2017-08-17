package taxi.hierarchies.tasks.dropoff;

import java.util.ArrayList;
import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffAgent;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffPassenger;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;
import taxi.hierarchies.tasks.dropoff.DropoffActionType.DropoffAction;

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
		
		if(a.actionName().startsWith(TaxiDropoffDomain.ACTION_DROPOFF)){
			dropoff(state, (DropoffAction)a, tps);
		}
		return tps;
	}

	/**
	 * put the passenger that is in the taxi on a depot 
	 * @param s
	 * @param tps
	 */
	public void dropoff(TaxiDropoffState s, DropoffAction a, List<StateTransitionProb> tps){
		String taxiLocation = (String) s.getTaxiAtt(TaxiDropoffDomain.ATT_CURRENT_LOCATION);
		String passenger = (String)a.getPassenger();
		boolean inTaxi = (boolean) s.getPassengerAtt(passenger, TaxiDropoffDomain.ATT_IN_TAXI);
		TaxiDropoffState ns = s.copy();
		
		//if some one is in taxi and it is at depot
		if(inTaxi && !taxiLocation.equals(TaxiDropoffDomain.ON_ROAD)){
			TaxiDropoffPassenger np = ns.touchPassenger(passenger);
			np.set(TaxiDropoffDomain.ATT_IN_TAXI, false);

			// iterate through every passenger except the one that was just dropped off and see if taxi is empty
			boolean passengersInTaxi = false;
			for(String p : s.getPassengers()) {
				boolean thisPinT = (boolean) s.getPassengerAtt(p, TaxiDropoffDomain.ATT_IN_TAXI);
				if ((!p.equals(passenger)) && thisPinT) {
					passengersInTaxi = true;
					break;
				}
			}

			// after iterating through passengers, if none are in taxi
			if (!passengersInTaxi) {
				TaxiDropoffAgent nt = ns.touchTaxi();
				nt.set(TaxiDropoffDomain.ATT_TAXI_OCCUPIED, false);
			}
		}
		tps.add(new StateTransitionProb(ns, 1));
	}
}
