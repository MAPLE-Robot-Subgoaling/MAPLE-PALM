package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import taxi.hierGen.Task5.state.TaxiHierGenTask5State;

public class HierGenTask5Action implements Action {

	private int goalX, goalY;

	public HierGenTask5Action(int x, int y){
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
		return new HierGenTask5Action(goalX, goalY);
	}

	@Override
	public boolean equals(Object other){
		if(this == other) return true;
		if(other == null || getClass() != other.getClass()) return false;

		HierGenTask5Action act = (HierGenTask5Action) other ;
		return goalX == act.getGoalX() && goalY == act.getGoalY();
	}

	@Override
	public int hashCode(){
		return actionName().hashCode();
	}

	@Override
	public String toString(){
		return actionName();
	}
}
