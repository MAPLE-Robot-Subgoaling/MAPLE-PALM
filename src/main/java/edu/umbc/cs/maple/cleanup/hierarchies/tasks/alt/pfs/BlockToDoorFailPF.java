package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class BlockToDoorFailPF extends PropositionalFunction {

    public BlockToDoorFailPF(){
        super("BlockToDoorFailPF", new String[]{});
    }

    public BlockToDoorFailPF(String name, String[] parameterClasses) {
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
//        if(object == null) object = state.object(MoveMapper.moveBlockTargetAlias);
        if (object == null || door == null) {
            // it doesn't exist -- you failed
            return true;
        }
        int bx = (int) object.get(ATT_X);
        int by = (int) object.get(ATT_Y);
        boolean inTheDoor = state.isObjectInDoor(object, (CleanupDoor) door);
        boolean inSomeDoor = state.doorContainingPoint(bx, by) != null;
        return !inTheDoor && inSomeDoor;
    }

}