package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockToDoorFailPF;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockToDoorGoalPF;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.Helpers;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class BlockToDoorActionType extends ObjectParameterizedActionType {

    public BlockToDoorActionType(){
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
        String doorName = params[1];
        ObjectInstance door = state.object(doorName);
        for (CleanupBlock block : state.getBlocks().values()) {
            int bx = (int) block.get(ATT_X);
            int by = (int) block.get(ATT_Y);
            boolean anyBlockInDoorAlready = CleanupState.regionContainsPoint(door, bx, by, true);
            if (anyBlockInDoorAlready) { return false; }
        }
        boolean inDoor = BlockToDoorGoalPF.isTrue(state, params);
        boolean inFailureCase = BlockToDoorFailPF.isTrue(state, params);
        return !inDoor && !inFailureCase;
    }

}
