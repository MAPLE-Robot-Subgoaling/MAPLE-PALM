package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class MoveBlockToDoorFailPF extends PropositionalFunction {

    public MoveBlockToDoorFailPF(){
        super("", new String[]{});
        name = this.getClass().getSimpleName();
    }

    public MoveBlockToDoorFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        if (state.numObjects() == 0) { return false; }
        String objectName = params[0];
        String doorName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance door = state.object(doorName);
        if (object == null) {
            return true;
        }
        if (door == null) {
            return true;
        }
        int ox = (int) object.get(ATT_X);
        int oy = (int) object.get(ATT_Y);
        boolean inGoalDoor = CleanupState.regionContainsPoint(door, ox, oy, true);
        boolean inAnyDoor = state.isObjectInAnyDoor(object);
        return inAnyDoor && !inGoalDoor;
    }

}