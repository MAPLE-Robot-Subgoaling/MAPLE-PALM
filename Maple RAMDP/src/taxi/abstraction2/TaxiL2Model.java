package taxi.abstraction2;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.Taxi;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction2.GetActionType.GetAction;
import taxi.abstraction2.PutActionType.PutAction;
import taxi.abstraction2.state.TaxiL2Passenger;
import taxi.abstraction2.state.TaxiL2State;
import taxi.state.TaxiPassenger;

public class TaxiL2Model implements FullStateModel {

	private double fickleChangeGoalProbaility;
	private boolean fickle;
	
	public TaxiL2Model(boolean fickle, double fickleprob) {
		this.fickle = fickle;
		this.fickleChangeGoalProbaility = fickleprob;
	}
	
	@Override
	public State sample(State s, Action a) {
		List<StateTransitionProb> stpList = this.stateTransitions(s,a);
        double roll = Math.random();
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
		int action = actionInd(a);
		TaxiL2State state = (TaxiL2State) s;
	
		if(action == TaxiL2.IND_GET){
			get(state, (GetAction) a, tps);
		}else if(action == TaxiL2.IND_PUT){
			put(state, (PutAction) a , tps);
		}
		return tps;
	}

	public void get(TaxiL2State s, GetAction a, List<StateTransitionProb> tps){
		String passegerName = a.getPassenger();
		TaxiL2State ns = s.copy();
		
		TaxiL2Passenger np = ns.touchPassenger(passegerName);
		np.set(TaxiL2.ATT_IN_TAXI, true);
		np.set(TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE, true);
		np.set(TaxiL2.ATT_JUST_PICKED_UP, true);
		tps.add(new StateTransitionProb(ns, 1));
	}
	
	public void put(TaxiL2State s, PutAction a, List<StateTransitionProb> tps){
		String goalLoaction = a.getGoalLocation();
		TaxiL2State ns = s.copy();
		
		int passengersAtGoal = 0;
		for(String passengerNamer : s.getPassengers()){
			String pLocation = (String) s.getPassengerAtt(passengerNamer, TaxiL2.ATT_CURRENT_LOCATION);
			boolean inTaxi = (boolean) s.getPassengerAtt(passengerNamer, TaxiL2.ATT_IN_TAXI);
			if(goalLoaction.equals(pLocation) && ! inTaxi)
				passengersAtGoal++;
		}
		
		for(String passengerName : s.getPassengers()){
			boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, TaxiL2.ATT_IN_TAXI);
			if(inTaxi){
				//change location and remove from taxi

				if(passengersAtGoal == 0){
					TaxiL2Passenger np = ns.touchPassenger(passengerName);
					np.set(TaxiL2.ATT_CURRENT_LOCATION, goalLoaction);
					np.set(TaxiL2.ATT_IN_TAXI, false);
				}
				
				//fickle goal
				if(fickle){
					String passengerLocation = (String) s.getPassengerAtt(passengerName, TaxiL2.ATT_CURRENT_LOCATION);
					boolean justPickedUp = (boolean) s.getPassengerAtt(passengerName, TaxiL2.ATT_JUST_PICKED_UP);
					if(justPickedUp){
						double p = fickleChangeGoalProbaility / (s.getLocations().length - 1);
						TaxiL2Passenger np = ns.touchPassenger(passengerName);
						np.set(TaxiL2.ATT_JUST_PICKED_UP, false);
						for(String locationName : s.getLocations()){
							TaxiL2State nfickles = ns.copy();
							
							if(locationName.equals(passengerLocation)){
								tps.add(new StateTransitionProb(nfickles, (1 - fickleChangeGoalProbaility)));
							}else{
								TaxiL2Passenger nfp = nfickles.touchPassenger(passengerName);
								nfp.set(TaxiL2.ATT_GOAL_LOCATION, locationName);
								tps.add(new StateTransitionProb(nfickles, p));
							}
						}
						return;
					}
				}
			}
		}
		
		tps.add(new StateTransitionProb(ns, 1));
	}
	
	public int actionInd(Action a){
		String aname = a.actionName();
		if(aname.startsWith(TaxiL2.ACTION_GET))
			return TaxiL2.IND_GET;
		else if(aname.startsWith(TaxiL2.ACTION_PUT))
			return TaxiL2.IND_PUT;
		throw new RuntimeException("Invalid action " + aname);
	}
}
