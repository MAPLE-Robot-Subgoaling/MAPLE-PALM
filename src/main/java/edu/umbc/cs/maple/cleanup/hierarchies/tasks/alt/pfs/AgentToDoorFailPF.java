package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class AgentToDoorFailPF extends PropositionalFunction {

    public AgentToDoorFailPF(){
        super("AgentToDoorFailPF", new String[]{});
    }

    public AgentToDoorFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        String doorName = params[0];
        ObjectInstance agent = state.getAgent();
        ObjectInstance door = state.object(doorName);
        if (door == null) {
            return false;
        }
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        return !state.isObjectInDoor(agent, (CleanupDoor) door) && state.doorContainingPoint(ax, ay) != null;
    }

}