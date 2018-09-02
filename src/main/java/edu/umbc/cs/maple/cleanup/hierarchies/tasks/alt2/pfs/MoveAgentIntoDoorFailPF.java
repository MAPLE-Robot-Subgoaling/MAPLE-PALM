package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class MoveAgentIntoDoorFailPF extends PropositionalFunction {

    public MoveAgentIntoDoorFailPF(){
        super("", new String[]{});
        name = this.getClass().getSimpleName();
    }

    public MoveAgentIntoDoorFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        if (state.numObjects() == 0) { return false; }
        String doorName = params[0];
        ObjectInstance agent = state.getAgent();
        ObjectInstance door = state.object(doorName);
        if (door == null) {
            return true;
        }
        boolean inAnyDoor = state.isObjectInAnyDoor(agent);
        boolean inGoalDoor = MoveAgentIntoDoorGoalPF.isTrue(state, params);
        return inAnyDoor && !inGoalDoor;
    }

}