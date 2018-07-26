package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.action.Action;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;
import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask5Action;

public class MoveAgentXYAction implements Action {
    private int goalX, goalY;

    public MoveAgentXYAction(int x, int y){
        this.goalX = x;
        this.goalY = y;
    }

    public int getGoalY() {
        return goalY;
    }

    public int getGoalX() {

        return goalX;
    }

    public String actionName() {
        return StringFormat.join("moveAgentXY", Integer.toString(goalX), Integer.toString(goalY));
    }


    public burlap.mdp.core.action.Action copy() {
        return new MoveAgentXYAction(goalX, goalY);
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
