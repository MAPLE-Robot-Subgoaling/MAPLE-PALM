package taxi.hiergen.actions;

import burlap.mdp.core.action.Action;
import hierarchy.framework.StringFormat;

import static taxi.TaxiConstants.ACTION_TASK_5;

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
        return StringFormat.join(ACTION_TASK_5, Integer.toString(goalX), Integer.toString(goalY));
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
