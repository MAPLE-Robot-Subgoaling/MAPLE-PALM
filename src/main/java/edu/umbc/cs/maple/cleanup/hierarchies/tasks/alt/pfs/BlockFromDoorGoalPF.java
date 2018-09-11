package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class BlockFromDoorGoalPF extends PropositionalFunction {

    public BlockFromDoorGoalPF(){
        super("BlockFromDoorGoalPF", new String[]{});
    }

    public BlockFromDoorGoalPF(String name, String[] parameterClasses) {
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
            return false;
        }
        return !state.isObjectInDoor(object, (CleanupDoor) door);
    }

}