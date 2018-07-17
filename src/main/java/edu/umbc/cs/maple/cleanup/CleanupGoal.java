package edu.umbc.cs.maple.cleanup;

import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.config.DomainGoal;

import java.util.List;

public class CleanupGoal extends DomainGoal<CleanupGoalDescription> {


    public CleanupGoal(List<CleanupGoalDescription> goals) {this.goalDescriptions = goals;}

    public CleanupGoal() {

    }

    @Override
    public boolean satisfies(State s) {
        for (int i = 0; i < goalDescriptions.size(); i++) {
            GroundedProp gp = new GroundedProp(goalDescriptions.get(i).getPf(), goalDescriptions.get(i).getParams());
            if (!gp.isTrue((CleanupState) s)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String out = "";
        if(goalDescriptions!=null) {
            for (CleanupGoalDescription desc : goalDescriptions) {
                out += desc.toString();
            }
        }
        return out;
    }

}