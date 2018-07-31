package edu.umbc.cs.maple.cleanup.hierarchies.tasks.root;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.CleanupGoal;
import edu.umbc.cs.maple.cleanup.CleanupGoalDescription;
import edu.umbc.cs.maple.cleanup.CleanupTF;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.hierarchy.framework.AMDPRootGoalPF;

import java.util.List;

public class CleanupRootGoalPF extends AMDPRootGoalPF {

    public CleanupRootGoalPF(){
        super("root", new String[]{});
    }

    public CleanupRootGoalPF(String name, CleanupGoal goal) {
        super(name, new String[]{});
        this.goal = goal;
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        return CleanupTF.atGoal((CleanupState) state, (CleanupGoal) this.goal);
    }

}
