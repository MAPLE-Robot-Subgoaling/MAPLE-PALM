package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockThruDoorGoalPF;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.Helpers;

public class BlockThruDoorActionType extends ObjectParameterizedActionType {

    public BlockThruDoorActionType() {
        super("ERRORNOTSET", new String[]{});
    }

    public void setParameterClasses(String[] parameterClasses) {
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
        boolean inDoorAlready = BlockThruDoorGoalPF.isTrue(state, params);
        boolean agentAdjacent = Cleanup.isAdjacent(state, params);
        return !inDoorAlready && agentAdjacent;
    }

}