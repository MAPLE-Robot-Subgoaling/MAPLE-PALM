package taxi.abstraction1;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.Taxi;
import taxi.abstraction1.NavigateActionType.NavigeteAction;
import taxi.abstraction1.state.TaxiL1Agent;
import taxi.abstraction1.state.TaxiL1Passenger;
import taxi.abstraction1.state.TaxiL1State;

public class TaxiL1Model implements FullStateModel {

	private double fickleChangeGoalProbaility;
	private boolean fickle;
	
	public TaxiL1Model(boolean fickle, double fickleprob) {
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
		TaxiL1State state = (TaxiL1State) s;
		
		if(action == TaxiL1.IND_NAVIGATE){
			navigate(state, (NavigeteAction) a, tps);
		}else if(action == TaxiL1.IND_L1PICKUP){
			pickup(state, a, tps);
		}else if(action == TaxiL1.IND_L1DROPOFF){
			dropoff(state, a, tps);
		}
		return tps;
	}

	public void navigate(TaxiL1State s, NavigeteAction a, List<StateTransitionProb> tps){
		String goal = a.getGoalLocation();
		
		TaxiL1State ns = s.copy();
		TaxiL1Agent taxi = ns.touchTaxi();
		taxi.set(TaxiL1.ATT_CURRENT_LOCATION, goal);
		
		for(String passengerName : s.getPassengers()){
			boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
			TaxiL1Passenger np = ns.touchPassenger(passengerName);
			if(inTaxi){
				np.set(TaxiL1.ATT_CURRENT_LOCATION, goal);
				break;
			}
		}
		
		if(fickle){
			for(String passengerName: s.getPassengers()){
				boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
				String passGoal = (String) s.getPassengerAtt(passengerName, 
						TaxiL1.ATT_GOAL_LOCATION);
				if(inTaxi){
					for(String locName: s.getLocations()){
						TaxiL1State nfickles = ns.copy();
						
						if(passGoal.equals(locName)){
							tps.add(new StateTransitionProb(nfickles, 1 - fickleChangeGoalProbaility));
						}else{
							TaxiL1Passenger np = nfickles.touchPassenger(passengerName);
							np.set(TaxiL1.ATT_GOAL_LOCATION, locName);
							tps.add(new StateTransitionProb(nfickles, fickleChangeGoalProbaility 
									/ (s.getLocations().length - 1)));
						}
					}
					continue;
				}
			}
		}
		tps.add(new StateTransitionProb(ns, 1.));		
	}
	
	public void pickup(TaxiL1State s, Action a, List<StateTransitionProb> tps){
		String taxiLocation = (String) s.getTaxiAtt(TaxiL1.ATT_CURRENT_LOCATION);
		boolean taxiOccupied = (boolean) s.getTaxiAtt(TaxiL1.ATT_TAXI_OCCUPIED);
		
		//if no one is in taxi and it is at depot
		if(!taxiOccupied && !taxiLocation.equals(TaxiL1.ON_ROAD)){
			for(String passengerName : s.getPassengers()){
				String passengerLocation = (String) s.getPassengerAtt(passengerName, TaxiL1.ATT_CURRENT_LOCATION);
				
				if(taxiLocation.equals(passengerLocation)){
					TaxiL1State ns = s.copy();
					TaxiL1Passenger np = ns.touchPassenger(passengerName);
					np.set(Taxi.ATT_IN_TAXI, true);
					np.set(TaxiL1.ATT_PICKED_UP_AT_LEAST_ONCE, true);
					
					TaxiL1Agent nt = ns.touchTaxi();
					nt.set(TaxiL1.ATT_TAXI_OCCUPIED, true);
					
					tps.add(new StateTransitionProb(ns, 1.));
					return;
				}
			}
		}
		tps.add(new StateTransitionProb(s.copy(), 1));
	}

	public void dropoff(TaxiL1State s, Action a, List<StateTransitionProb> tps){
		String taxiLocation = (String) s.getTaxiAtt(TaxiL1.ATT_CURRENT_LOCATION);
		boolean taxiOccupied = (boolean) s.getTaxiAtt(TaxiL1.ATT_TAXI_OCCUPIED);
		
		//if some one is in taxi and it is at depot
		if(taxiOccupied && !taxiLocation.equals(TaxiL1.ON_ROAD)){
			for(String passengerName : s.getPassengers()){
				String passengerLocation = (String) s.getPassengerAtt(passengerName, TaxiL1.ATT_CURRENT_LOCATION);
				
				if(taxiLocation.equals(passengerLocation)){
					TaxiL1State ns = s.copy();
					TaxiL1Passenger np = ns.touchPassenger(passengerName);
					np.set(Taxi.ATT_IN_TAXI, false);
					
					TaxiL1Agent nt = ns.touchTaxi();
					nt.set(TaxiL1.ATT_TAXI_OCCUPIED, false);
					
					tps.add(new StateTransitionProb(ns, 1));
					return;
				}
			}
		}
		tps.add(new StateTransitionProb(s.copy(), 1));
	}
	
	public int actionInd(Action a){
		String aname = a.actionName();
		if(aname.startsWith(TaxiL1.ACTION_NAVIGATE))
			return TaxiL1.IND_NAVIGATE;
		else if(aname.equals(TaxiL1.ACTION_L1PICKUP))
			return TaxiL1.IND_L1PICKUP;
		else if(aname.equals(TaxiL1.ACTION_L1DROPOFF))
			return TaxiL1.IND_L1DROPOFF;
		throw new RuntimeException("Invalid action " + aname);
	}
}
