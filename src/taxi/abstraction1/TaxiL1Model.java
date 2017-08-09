package taxi.abstraction1;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.Taxi;
import taxi.abstraction1.NavigateActionType.NavigeteAction;
import taxi.abstraction1.PickupActionType.PickupAction;
import taxi.abstraction1.DropOffActionType.DropOffAction;
import taxi.abstraction1.state.TaxiL1Agent;
import taxi.abstraction1.state.TaxiL1Passenger;
import taxi.abstraction1.state.TaxiL1State;

public class TaxiL1Model implements FullStateModel {

	/**
	 * the probability the passengers change their goal
	 */
	private double fickleChangeGoalProbaility;
	
	/**
	 * whether the passengers are fickle
	 */
	private boolean fickle;
	
	/**
	 * create a taxi abstraction 1 model
	 * @param fickle whether the passengers are fickle
	 * @param fickleprob the probability te passengers change goal when just picked up
	 */
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
			pickup(state, (PickupAction) a, tps);
		}else if(action == TaxiL1.IND_L1DROPOFF){
			dropoff(state, (DropOffAction) a, tps);
		}
		return tps;
	}

	/**
	 * put the taxi at the goal location of the action
	 * @param s the current state
	 * @param a the nav action
	 * @param tps the list of outcomes to add to
	 */
	public void navigate(TaxiL1State s, NavigeteAction a, List<StateTransitionProb> tps){
		String goal = a.getGoalLocation();
		
		TaxiL1State ns = s.copy();
		TaxiL1Agent taxi = ns.touchTaxi();
		taxi.set(TaxiL1.ATT_CURRENT_LOCATION, goal);
		
		for(String passengerName : s.getPassengers()){
			boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
			if(inTaxi){
				TaxiL1Passenger np = ns.touchPassenger(passengerName);
				np.set(TaxiL1.ATT_CURRENT_LOCATION, goal);
				break;
			}
		}
		
		if(fickle){
			boolean passengerChanged = false;
			for(String passengerName: s.getPassengers()){
				boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
				boolean justPickedUp = (boolean) s.getPassengerAtt(passengerName, TaxiL1.ATT_JUST_PICKED_UP);
				String passGoal = (String) s.getPassengerAtt(passengerName, 
						TaxiL1.ATT_GOAL_LOCATION);
				if(inTaxi && justPickedUp){
					passengerChanged = true;
					TaxiL1Passenger np = ns.touchPassenger(passengerName);
					np.set(TaxiL1.ATT_JUST_PICKED_UP, false);
					for(String locName: s.getLocations()){
						TaxiL1State nfickles = ns.copy();
						
						if(passGoal.equals(locName)){
							tps.add(new StateTransitionProb(nfickles, 1 - fickleChangeGoalProbaility));
						}else{
							TaxiL1Passenger npf = nfickles.touchPassenger(passengerName);
							npf.set(TaxiL1.ATT_GOAL_LOCATION, locName);
							tps.add(new StateTransitionProb(nfickles, fickleChangeGoalProbaility 
									/ (s.getLocations().length - 1)));
						}
					}
					break;
				}
			}
			if(!passengerChanged){
				tps.add(new StateTransitionProb(ns, 1));
			}
		}else{
			tps.add(new StateTransitionProb(ns, 1.));
		}
	}
	
	/**
	 * put passenger in taxi if it is at the taxi and taxi is open
	 * @param s the current state
	 * @param tps the list of outcomes to add to
	 */
	public void pickup(TaxiL1State s, PickupAction a, List<StateTransitionProb> tps){
		String passengerName = a.getPassenger();
		String passengerLocation = (String) s.getPassengerAtt(passengerName, TaxiL1.ATT_CURRENT_LOCATION);
		String taxiLocation = (String) s.getTaxiAtt(TaxiL1.ATT_CURRENT_LOCATION);
		boolean taxiOccupied = (boolean) s.getTaxiAtt(TaxiL1.ATT_TAXI_OCCUPIED);
		TaxiL1State ns = s.copy();

		//if taxi is at depot
		if(!taxiLocation.equals(TaxiL1.ON_ROAD) && taxiLocation.equals(passengerLocation)){
            TaxiL1Passenger np = ns.touchPassenger(passengerName);
            np.set(Taxi.ATT_IN_TAXI, true);
            np.set(TaxiL1.ATT_PICKED_UP_AT_LEAST_ONCE, true);
            if(fickle){
                np.set(TaxiL1.ATT_JUST_PICKED_UP, true);
            }

            TaxiL1Agent nt = ns.touchTaxi();
            nt.set(TaxiL1.ATT_TAXI_OCCUPIED, true);
		}
		tps.add(new StateTransitionProb(ns, 1));
	}

	/**
	 * put the passenger that is in the taxi on a depot 
	 * @param s
	 * @param tps
	 */
	public void dropoff(TaxiL1State s, DropOffAction a, List<StateTransitionProb> tps){
		String passengerName = a.getPassenger();
		boolean passengerInTaxi = (boolean)s.getPassengerAtt(passengerName, TaxiL1.ATT_IN_TAXI);
		String taxiLocation = (String) s.getTaxiAtt(TaxiL1.ATT_CURRENT_LOCATION);
		TaxiL1State ns = s.copy();

		//if some one is in taxi and it is at depot
		if(!taxiLocation.equals(TaxiL1.ON_ROAD) && passengerInTaxi) {
			TaxiL1Passenger np = ns.touchPassenger(passengerName);
			np.set(Taxi.ATT_IN_TAXI, false);

			boolean passengersInTaxi = false;
			// iterate through every passenger except the one that was just dropped off and see if taxi is empty
			for(String passenger : s.getPassengers()) {
				boolean inTaxi = (boolean) s.getPassengerAtt(passenger, Taxi.ATT_IN_TAXI);
				if ((!passenger.equals(passengerName)) && inTaxi) {
					passengersInTaxi = true;
					break;
				}
			}

			// after iterating through passengers, if none are in taxi
			if (!passengersInTaxi) {
				TaxiL1Agent nt = ns.touchTaxi();
				nt.set(Taxi.ATT_TAXI_OCCUPIED, false);
			}
		}
		tps.add(new StateTransitionProb(ns, 1));
	}

	/**
	 * map a action to its number
	 * @param a the action
	 * @return the number that represents the action
	 */
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
