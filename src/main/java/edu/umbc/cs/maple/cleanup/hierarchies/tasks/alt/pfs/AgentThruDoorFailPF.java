package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class AgentThruDoorFailPF extends PropositionalFunction {

    public AgentThruDoorFailPF(){
        super("AgentThruDoorFailPF", new String[]{});
    }

    public AgentThruDoorFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
//        boolean notInDoor = AgentThruDoorGoalPF.isTrue(state, params);
//        return !notInDoor;
        return false;
    }

}