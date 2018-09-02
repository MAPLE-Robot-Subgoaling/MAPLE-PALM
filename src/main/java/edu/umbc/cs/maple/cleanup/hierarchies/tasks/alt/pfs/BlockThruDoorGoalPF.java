package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class BlockThruDoorGoalPF extends PropositionalFunction {

    public BlockThruDoorGoalPF(){
        super("BlockThruDoorGoalPF", new String[]{});
    }

    public BlockThruDoorGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        String objectName = params[0];
        String doorName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance door = state.object(doorName);
        if (door == null) {
            return false;
        }
        if (object == null) {
            return false;
        }
        return !state.isObjectInDoor(object, (CleanupDoor) door);
    }

}