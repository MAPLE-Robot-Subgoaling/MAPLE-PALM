package edu.umbc.cs.maple.cleanup;

import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.config.DomainGoal;

import java.util.ArrayList;
import java.util.List;

public class CleanupGoal extends DomainGoal {

    public List<CleanupGoalDescription> goals=new ArrayList();

    public CleanupGoal(List<CleanupGoalDescription> goals) {
        this.goals = goals;
    }

    public CleanupGoal() {

    }

    public List<CleanupGoalDescription> getGoals() {
        return goals;
    }

    public void setGoals(List<CleanupGoalDescription> goals) {
        this.goals = goals;
    }

    @Override
    public boolean satisfies(State s) {
        for (int i = 0; i < goals.size(); i++) {
            GroundedProp gp = new GroundedProp(goals.get(i).getPf(), goals.get(i).getParams());
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