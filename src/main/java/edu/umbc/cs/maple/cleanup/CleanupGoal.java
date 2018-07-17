package edu.umbc.cs.maple.cleanup;

import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.config.DomainGoal;

public class CleanupGoal extends DomainGoal {

    public CleanupGoalDescription[] goals = {};

    public CleanupGoal(CleanupGoalDescription[] goals) {
        this.goals = goals;
    }

    public CleanupGoal() {

    }

    public CleanupGoalDescription[] getGoals() {
        return goals;
    }

    public void setGoals(CleanupGoalDescription[] goals) {
        this.goals = goals;
    }

    @Override
    public boolean satisfies(State s) {
        for (int i = 0; i < goals.length; i++) {
            GroundedProp gp = new GroundedProp(goals[i].getPf(), goals[i].getParams());
            if (!gp.isTrue((CleanupState) s)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String out = "";
        for (CleanupGoalDescription desc : goals) {
            out += desc.toString();
        }
        return out;
    }

}