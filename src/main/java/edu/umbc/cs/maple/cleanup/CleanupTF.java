package edu.umbc.cs.maple.cleanup;

import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import java.util.List;

public class CleanupTF implements TerminalFunction {

    protected CleanupGoal goal;

    public CleanupTF() {
        // for de/serialization
    }

    public CleanupTF(CleanupGoal goal) {
        this.goal = goal;
    }


    public static boolean atGoal(CleanupState state, CleanupGoal goal) {
        List<CleanupGoalDescription> goals = goal.getGoalDescriptions();
        for (CleanupGoalDescription goalDescription : goals) {
            PropositionalFunction pf = goalDescription.getPf();
            String[] pfParams = goalDescription.getParams();
            if (!pf.isTrue(state, pfParams)) {
                return false;
            }
        }
        // only reach if all true, or no goals (trivial case)
        return true;
    }

    public CleanupGoal getGoal() {
        return goal;
    }

    public void setGoal(CleanupGoal goal) {
        this.goal = goal;
    }

    @Override
    public boolean isTerminal(State s) {
        return atGoal((CleanupState) s, goal);
    }

}
