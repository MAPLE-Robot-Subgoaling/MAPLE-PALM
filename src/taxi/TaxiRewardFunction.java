package taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import taxi.state.TaxiState;

public class TaxiRewardFunction implements RewardFunction{

	/**
	 * the reward for taking a action
	 */
	private double stepReward;
	
	/**
	 * the reward for a impossible pickup or dropoff action
	 */
	private double illegalActionReward;
	
	/**
	 * the reward for completing the goal
	 */
	private double goalReward;
	
	/**
	 * the taxi terminal function
	 */
	private TerminalFunction tf;
	
	/**
	 * use the default rewards
	 */
	public TaxiRewardFunction() {
		stepReward = -1.0;
		illegalActionReward = -10;
		goalReward = 20;
		tf = new TaxiTerminalFunction();
	}
	
	/**
	 * use custom rewards
	 * @param stepR the reward for a action
	 * @param illegalR the reward for a impossible pickup or dropoff
	 * @param goalR the reward for completing the goal
	 */
	public TaxiRewardFunction(double stepR, double illegalR, double goalR){
		stepReward = stepR;
		illegalActionReward = illegalR;
		goalReward = goalR;
		tf = new TaxiTerminalFunction();
	}
	
	@Override
	public double reward(State s, Action a, State sprime) {
		TaxiState state = (TaxiState) s;
		
		if(tf.isTerminal(sprime))
			return goalReward + stepReward;
		
		boolean taxiOccupied = (boolean) state.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);
		int tx = (int) state.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) state.getTaxiAtt(Taxi.ATT_Y);
		
		//illegal pickup when no passenger at taxi's location or if taxi is occupied
		if(a.actionName().equals(Taxi.ACTION_PICKUP)){
			if(taxiOccupied)
				return stepReward + illegalActionReward;
			
			boolean passengerAtTaxi = false;
			for(String passengerName : state.getPassengers()){
				int px = (int) state.getPassengerAtt(passengerName, Taxi.ATT_X);
				int py = (int) state.getPassengerAtt(passengerName, Taxi.ATT_Y);
				if(px == tx && py == ty){
					passengerAtTaxi = true;
					break;
				}
			}
			
			if(!passengerAtTaxi)
				return stepReward + illegalActionReward;
		}
		//illegal dropoff if not at depot or passenger not in taxi
		else if(a.actionName().equals(Taxi.ACTION_DROPOFF)){
			if(!taxiOccupied)
				return stepReward + illegalActionReward;
			
			// if taxi/passenger is not at depot
			boolean taxiAtDepot = false;
			for(String locName : state.getLocations()){
				int lx = (int) state.getLocationAtt(locName, Taxi.ATT_X);
				int ly = (int) state.getLocationAtt(locName, Taxi.ATT_Y);
				if(tx == lx && ty == ly){
					taxiAtDepot = true;
					break;
				}
			}


			if(!taxiAtDepot)
				return stepReward + illegalActionReward;
		}
		
		return stepReward;
	}
}
