package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.OnlyThisRoomMapper;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.Helpers;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class MoveAgentIntoRoomActionType extends ObjectParameterizedActionType {

    public MoveAgentIntoRoomActionType(){
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
        String doorName = params[0];
        String roomName = params[1];
        ObjectInstance agent = state.getAgent();
        ObjectInstance door = state.object(doorName);
        ObjectInstance room = state.object(roomName);
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        boolean agentInDoor = CleanupState.regionContainsPoint(door, ax, ay, true);
        if (!agentInDoor) { return false; }
        // the agent and door share coordinates
        boolean doorNextToRoom = CleanupState.regionContainsPoint(room, ax, ay, true);
        if (!doorNextToRoom) { return false; }
        return true;
    }

}