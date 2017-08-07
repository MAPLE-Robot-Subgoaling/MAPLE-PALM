package taxi;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.state.TaxiAgent;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

public class TaxiModel implements FullStateModel{

	/**
	 * the array saying how the probabilities are distributed
	 */
	private double[][] moveProbability;
	
	/**
	 * probability the passenger just picked up changes their goal 
	 */
	private double fickleChangeGoalProbaility;
	
	/**
	 * whether the passengers are fickle
	 */
	private boolean fickle;

	/**
	 * create a taxi model
	 * @param moveprob array of movement probabilities
	 * @param fickle whether passengers are fickle
	 * @param fickleprob probability the passengers are fickle
	 */
	public TaxiModel(double[][] moveprob, boolean fickle, double fickleprob) {
		this.moveProbability = moveprob;
		this.fickleChangeGoalProbaility = fickleprob;
		this.fickle = fickle;
	}
	
	/**
	 * creates a stochastic non fickle taxi model
	 * @param moveprob the array of movement probabilities
	 */
	public TaxiModel(double[][] moveprob){
		this.moveProbability = moveprob;
		this.fickle = false;
	}
	
	@Override
	public State sample(State s, Action a) {
		 List<StateTransitionProb> stpList = this.stateTransitions(s,a);
         double roll = Math.random();
//         System.out.println(roll);
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
		TaxiState taxiS = (TaxiState) s;
		
		if(action <= Taxi.IND_WEST){
			movement(taxiS, action, tps);
		}else if(action == Taxi.IND_DROPOFF){
			dropoff(taxiS, tps);
		}else if(action == Taxi.IND_PICKUP){
			pickup(taxiS, ((PickupActionType.PickupAction)a).getPassenger(), tps);
		}
		
		return tps;
	}

	/**
	 * add all resulting states given a movement action
	 * @param s the current state
	 * @param action the index of the selected movement action
	 * @param tps a list of state transition probabilities to add to
	 */
	public void movement(TaxiState s, int action, List<StateTransitionProb> tps){
		double[] moveProbabilities = this.moveProbability[action];
		
		int tx = (int) s.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) s.getTaxiAtt(Taxi.ATT_Y);
		
		for(int outcome = 0; outcome < moveProbabilities.length; outcome++){
			double p = moveProbabilities[outcome];
			if(p == 0)
				continue;
			
			int dx = 0, dy = 0;
			TaxiState ns = s.copy();
			
			//move in the given direction unless there are walls in the way
			if(outcome == Taxi.IND_NORTH){
				if(!ns.wallNorth()){
					dy = +1;
				}
			}else if(outcome == Taxi.IND_EAST){
				if(!ns.wallEast()){
					dx = +1;
				}
			}else if(outcome == Taxi.IND_SOUTH){
				if(!ns.wallSouth()){
					dy = -1;
				}
			}else if(outcome == Taxi.IND_WEST){
				if(!ns.wallWest()){
					dx = -1;
				}
			}
			
			int nx = tx + dx;
			int ny = ty + dy;
			TaxiAgent ntaxi = ns.touchTaxi();
			ntaxi.set(Taxi.ATT_X, nx);
			ntaxi.set(Taxi.ATT_Y, ny);
			
			//move any passenger that are in the taxi
			for(String passengerName : s.getPassengers()){
				boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
				
				if(inTaxi){
					TaxiPassenger np = ns.touchPassenger(passengerName);
					np.set(Taxi.ATT_X, nx);
					np.set(Taxi.ATT_Y, ny);
					break;
				}
			}
			
			if(fickle){
				//find a passenger in the taxi
				boolean passengerChanged = false;
				for(String passengerName : s.getPassengers()){
					boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
					boolean justPickedUp = (boolean) s.getPassengerAtt(passengerName, Taxi.ATT_JUST_PICKED_UP);
					String passGoal = (String) s.getPassengerAtt(passengerName, 
							Taxi.ATT_GOAL_LOCATION);					
					if(inTaxi && justPickedUp){
						passengerChanged = true;
						TaxiPassenger np = ns.touchPassenger(passengerName);
						np.set(Taxi.ATT_JUST_PICKED_UP, false);
						// may change goal
						for(String locName : s.getLocations()){
							TaxiState nfickles = ns.copy();
							
							//check if goal is the same as loc
							if(passGoal.equals(locName)){
								double prob = p * (1 - fickleChangeGoalProbaility);
								tps.add(new StateTransitionProb(nfickles, prob));
							}else{
								//set goal to loc
								TaxiPassenger npf = nfickles.touchPassenger(passengerName);
								npf.set(Taxi.ATT_GOAL_LOCATION, locName);
								tps.add(new StateTransitionProb(nfickles, p * (fickleChangeGoalProbaility
										/ (s.getLocations().length - 1))));
							}
						}
						break;
					}
				}
				if(!passengerChanged){
					tps.add(new StateTransitionProb(ns, p));					
				}
			}else{
				tps.add(new StateTransitionProb(ns, p));
			}
		}
	}
	
	/**
	 * put passenger at the taxi inside if no one else is inside
	 * @param s the current state
     * @param p the name of the passenger to pickup
	 * @param tps a list of state transition probabilities to add to
	 */
	public void pickup(TaxiState s, String p, List<StateTransitionProb> tps) {
		int tx = (int) s.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) s.getTaxiAtt(Taxi.ATT_Y);
		boolean taxiOccupied = (boolean) s.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);

		if (!taxiOccupied) {
			int px = (int) s.getPassengerAtt(p, Taxi.ATT_X);
			int py = (int) s.getPassengerAtt(p, Taxi.ATT_Y);
			boolean inTaxi = (boolean) s.getPassengerAtt(p, Taxi.ATT_IN_TAXI);

			if (tx == px && ty == py && !inTaxi) {
				// pick up
				TaxiState ns = s.copy();
				TaxiPassenger np = ns.touchPassenger(p);
				np.set(Taxi.ATT_IN_TAXI, true);
				np.set(Taxi.ATT_PICKED_UP_AT_LEAST_ONCE, true);
				if (fickle) {
					np.set(Taxi.ATT_JUST_PICKED_UP, true);
				}
				TaxiAgent ntaxi = ns.touchTaxi();
				ntaxi.set(Taxi.ATT_TAXI_OCCUPIED, true);

				tps.add(new StateTransitionProb(ns, 1.));
				return;
			}
			tps.add(new StateTransitionProb(s.copy(), 1.));
		}
	}
	
	/**
	 * put passenger down if the taxi is occupied and at a depot
	 * @param s the current state
	 * @param tps a list of state transition probabilities to add to
	 */
	public void dropoff(TaxiState s, List<StateTransitionProb> tps){
		int tx = (int) s.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) s.getTaxiAtt(Taxi.ATT_Y); 
		boolean taxiOccupied = (boolean) s.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);
		TaxiState ns = s.copy();

		if(taxiOccupied){
			for(String loc : s.getLocations()){
				int lx = (int) s.getLocationAtt(loc, Taxi.ATT_X);
				int ly = (int) s.getLocationAtt(loc, Taxi.ATT_Y);

				if( tx == lx && ty == ly){
					for(String passengerName : s.getPassengers()){
						int px = (int) s.getPassengerAtt(passengerName, Taxi.ATT_X);
						int py = (int) s.getPassengerAtt(passengerName, Taxi.ATT_Y);
						boolean inTaxi = (boolean) s.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
                        if(inTaxi){
                            TaxiPassenger np = ns.touchPassenger(passengerName);
                            np.set(Taxi.ATT_IN_TAXI, false);
                        }
                    }
				}
			}

            TaxiAgent ntaxi = ns.touchTaxi();
            ntaxi.set(Taxi.ATT_TAXI_OCCUPIED, false);
		}

		tps.add(new StateTransitionProb(ns, 1.));
	}
	
	/**
	 * map a action to its number
	 * @param a the action
	 * @return the number that represents the action
	 */
	public int actionInd(Action a){
		String aname = a.actionName();
		if(aname.equals(Taxi.ACTION_NORTH))
			return Taxi.IND_NORTH;
		else if(aname.equals(Taxi.ACTION_EAST))
			return Taxi.IND_EAST;
		else if(aname.equals(Taxi.ACTION_SOUTH))
			return Taxi.IND_SOUTH;
		else if(aname.equals(Taxi.ACTION_WEST))
			return Taxi.IND_WEST;
		else if(aname.startsWith(Taxi.ACTION_PICKUP))
			return Taxi.IND_PICKUP;
		else if(aname.equals(Taxi.ACTION_DROPOFF))
			return Taxi.IND_DROPOFF;
		throw new RuntimeException("Invalid action " + aname);
	}
}
