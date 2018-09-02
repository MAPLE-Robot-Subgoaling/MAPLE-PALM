package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.OnlyThisRoomMapper;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockFromDoorFailPF;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockFromDoorGoalPF;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.Helpers;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class MoveAgentIntoDoorActionType extends ObjectParameterizedActionType {

    public MoveAgentIntoDoorActionType(){
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
        // must be next to the door
        boolean agentIsAdjacentToDoor = Cleanup.isAdjacent(state, params);
        if (!agentIsAdjacentToDoor) return false;

        ObjectInstance door = state.object(params[0]);

        // must have no blocks in door
        for (CleanupBlock block : state.getBlocks().values()) {
            int bx = (int) block.get(ATT_X);
            int by = (int) block.get(ATT_Y);
            boolean blockInDoor = CleanupState.regionContainsPoint(door, bx, by, true);
            if (blockInDoor) { return false; }
        }

        return true;
    }

}