package cleanup.hierarchies.tasks.root;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import cleanup.CleanupGoal;
import cleanup.CleanupGoalDescription;

public class CleanupRootGoalPF extends PropositionalFunction {

    private CleanupGoal goal;

    public CleanupRootGoalPF(String name, CleanupGoal goal) {
        super(name, new String[]{});
        this.goal = goal;
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        CleanupGoalDescription[] goals = goal.getGoals();
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
