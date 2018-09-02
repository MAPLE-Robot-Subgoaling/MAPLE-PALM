package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.OnlyThisRoomMapper;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockToDoorFailPF;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockToDoorGoalPF;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.Helpers;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class MoveBlockToDoorActionType extends ObjectParameterizedActionType {

    public MoveBlockToDoorActionType(){
        super("ERRORNOTSET", new String[]{});
    }

    public void setParameterClasses(String[] parameterClasses){
        this.parameterClasses = parameterClasses;
    }

    public void setParameterOrderGroup(String[] parameterOrderGroup) {
        this.parameterOrderGroup = parameterOrderGroup;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        CleanupState state = (CleanupState) OnlyThisRoomMapper.mapper.mapState(s);
        String[] params = objectParameterizedAction.getObjectParameters();
        boolean anyNull = Helpers.anyParamsNull(state, params);
        if (anyNull) {
            return false;
        }

        ObjectInstance agent = state.getAgent();
        if (state.isObjectInAnyDoor(agent)) {
            return false;
        }

        String blockName = params[0];
        String doorName = params[1];
        ObjectInstance door = state.object(doorName);
        ObjectInstance block = state.object(blockName);
        int bx = (int) block.get(ATT_X);
        int by = (int) block.get(ATT_Y);
        boolean blockInAnyDoor = state.isObjectInAnyDoor(block);
//        boolean blockInDoor = CleanupState.regionContainsPoint(door, bx, by, true);
        if (blockInAnyDoor) {
            return false;
        }
        return true;
    }

}