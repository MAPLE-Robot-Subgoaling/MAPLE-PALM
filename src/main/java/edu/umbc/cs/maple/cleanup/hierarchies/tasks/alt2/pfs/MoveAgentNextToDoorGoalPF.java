package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class MoveAgentNextToDoorGoalPF extends PropositionalFunction {

    public MoveAgentNextToDoorGoalPF(){
        super("", new String[]{});
        name = this.getClass().getSimpleName();
    }

    public MoveAgentNextToDoorGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        if (state.numObjects() == 0) { return true; }
        String doorName = params[0];
        ObjectInstance agent = state.getAgent();
        ObjectInstance door = state.object(doorName);
        if (door == null) {
            return false;
        }
        boolean agentIsAdjacentToDoor = Cleanup.isAdjacent(state, params);
        if (agentIsAdjacentToDoor) return true;
        return false;
    }

}