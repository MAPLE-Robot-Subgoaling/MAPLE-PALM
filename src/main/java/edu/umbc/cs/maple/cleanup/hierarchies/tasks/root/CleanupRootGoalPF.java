package edu.umbc.cs.maple.cleanup.hierarchies.tasks.root;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.CleanupGoal;
import edu.umbc.cs.maple.cleanup.CleanupGoalDescription;

import java.util.List;

public class CleanupRootGoalPF extends PropositionalFunction {

    private CleanupGoal goal;

    public CleanupRootGoalPF(){
        super("root", new String[]{});
    }
    public CleanupRootGoalPF(String name, CleanupGoal goal) {
        super(name, new String[]{});
        this.goal = goal;
    }

    public void setGoal(CleanupGoal goal){
        this.goal=goal;
    }

    public CleanupGoal getGoal(){
        return goal;
    }
    @Override
    public boolean isTrue(OOState state, String[] params) {
        List<CleanupGoalDescription> goals = goal.getGoals();
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
}
