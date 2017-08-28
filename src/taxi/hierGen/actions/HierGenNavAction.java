package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import taxi.hierGen.Task5.state.TaxiHierGenTask5State;

public class HierGenNavAction implements Action {

	private int goalX, goalY;

	public HierGenNavAction(int x, int y){
		this.goalX = x;
		this.goalY = y;
	}

	public int getGoalY() {
		return goalY;
	}

	public int getGoalX() {

		return goalX;
	}

	@Override
	public String actionName() {
		return TaxiHierGenTask5State.ACTION_Task5_Action + "_" + goalX + "_" + goalY;
	}

	@Override
	public Action copy() {
		return new HierGenNavAction(goalX, goalY);
	}

	@Override
	public boolean equals(Object other){
		if(this == other) return true;
		if(other == null || getClass() != other.getClass()) return false;

		HierGenNavAction act = (HierGenNavAction) other ;
		return goalX == act.getGoalX() && goalY == act.getGoalY();
	}

	@Override
	public int hashCode(){
		return actionName().hashCode();
	}
}
