package taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import taxi.state.TaxiState;
import static taxi.TaxiConstants.*;

public class TaxiRewardFunction implements RewardFunction{

	/**
	 * the rewardTotal for taking a action
	 */
	private double stepReward;
	
	/**
	 * the rewardTotal for a impossible pickup or dropoff action
	 */
	private double illegalActionReward;
	
	/**
	 * the rewardTotal for completing the goal
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
		// scaled to fit goal at 1.0
		stepReward = -0.05;//-1;
		illegalActionReward = -0.5;//-10;
		goalReward = 1.0;//20;
		tf = new TaxiTerminalFunction();
	}
	
	/**
	 * use custom rewards
	 * @param stepR the rewardTotal for a action
	 * @param illegalR the rewardTotal for a impossible pickup or dropoff
	 * @param goalR the rewardTotal for completing the goal
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

		//goal rewardTotal when state is terminal
		if(tf.isTerminal(sprime))
			return goalReward + stepReward;
		
		boolean taxiOccupied = (boolean) state.getTaxiAtt(ATT_TAXI_OCCUPIED);
		int tx = (int) state.getTaxiAtt(ATT_X);
		int ty = (int) state.getTaxiAtt(ATT_Y);
		
		//illegal pickup when no passenger at taxi's location
		if(a.actionName().equals(ACTION_PICKUP)){

			boolean passengerAtTaxi = false;
			for(String passengerName : state.getPassengers()){
				int px = (int) state.getPassengerAtt(passengerName, ATT_X);
				int py = (int) state.getPassengerAtt(passengerName, ATT_Y);
				if(px == tx && py == ty){
					passengerAtTaxi = true;
					break;
				}
			}
			
			if(!passengerAtTaxi)
				return stepReward + illegalActionReward;
		}
		//illegal dropoff if not at depot or passenger not in taxi
		else if(a.actionName().startsWith(ACTION_PUTDOWN)){
			if(!taxiOccupied)
				return stepReward + illegalActionReward;
			
			// if taxi/passenger is not at depot
			boolean taxiAtDepot = false;
			for(String locName : state.getLocations()){
				int lx = (int) state.getLocationAtt(locName, ATT_X);
				int ly = (int) state.getLocationAtt(locName, ATT_Y);
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
