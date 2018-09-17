package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.OnlyThisRoomMapper;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.Helpers;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class PullBlockFromDoorActionType extends ObjectParameterizedActionType {

    public PullBlockFromDoorActionType(){
        super("ERRORNOTSET", new String[]{});
    }

    public void setParameterClasses(String[] parameterClasses){
        this.parameterClasses = parameterClasses;
    }

    public void setParameterOrderGroup(String[] parameterOrderGroup) {
        this.parameterOrderGroup = parameterOrderGroup;
    }

    //method to calculate one more in agent direction
    private int[] oneMore(int x, int y, ObjectInstance agent){
        String direction = (String) agent.get(Cleanup.ATT_DIR);
        //north
        if(direction.equals(Cleanup.directions[0]))
            y++;
            //south
        else if(direction.equals(Cleanup.directions[1]))
            y--;
            //east
        else if(direction.equals(Cleanup.directions[2]))
            x++;
            //west
        else if(direction.equals(Cleanup.directions[3]))
            x--;
        int[] answer = {x,y};
        return answer;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        CleanupState state = (CleanupState) s;//OnlyThisRoomMapper.mapper.mapState(s);
        String[] params = objectParameterizedAction.getObjectParameters();
        boolean anyNull = Helpers.anyParamsNull(state, params);
        if (anyNull) {
            return false;
        }

        ObjectInstance agent = state.getAgent();
        int[] next = oneMore((int)agent.get(ATT_X), (int) agent.get(ATT_Y),agent);
        int x = next[0];
        int y = next[1];
        //applicable only if agent is facing block
        if(!state.blockAt(x,y))
            return false;
        //or if another block behind block in door, agent able to face it w/o moving in position
        else if(!state.blockAt(oneMore(x,y,agent)[0], oneMore(x,y,agent)[1]))
            return false;

        if (state.isObjectInAnyDoor(agent)) {
            return false;
        }



        String blockName = params[0];
        String doorName = params[1];
        String roomName = params[2];
        ObjectInstance block = state.object(blockName);
        ObjectInstance door = state.object(doorName);
        ObjectInstance room = state.object(roomName);

        if (block instanceof LocationBlock) {
            String region = (String) block.get(ATT_REGION);
            if (!region.equals(doorName)) { return false; }
        } else {
            int bx = (int) block.get(ATT_X);
            int by = (int) block.get(ATT_Y);
            boolean blockInDoor = CleanupState.regionContainsPoint(door, bx, by, true);
            if (!blockInDoor) { return false; }
        }

        if (block instanceof LocationBlock) {
            boolean agentAdjacent = Cleanup.isAdjacent(state, new String[]{doorName});
            if (!agentAdjacent) { return false; }
        } else {
            boolean agentAdjacent = Cleanup.isAdjacent(state, params);
            if (!agentAdjacent) { return false; }
        }

        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
//        String direction = (String) agent.get(ATT_DIR);
//        int bx = (int) block.get(ATT_X);
//        int by = (int) block.get(ATT_Y);
//        if (direction.equals(ACTION_NORTH)) {
//            if (!(ax == bx && ay + 1 == by) ) { return false; }
//        } else if (direction.equals(ACTION_SOUTH)) {
//            if (!(ax == bx && ay - 1 == by) ) { return false; }
//        } else if (direction.equals(ACTION_EAST)) {
//            if (!(ax + 1 == bx && ay == by) ) { return false; }
//        } else if (direction.equals(ACTION_WEST)) {
//            if (!(ax - 1 == bx && ay == by) ) { return false; }
//        } else {
//            throw new RuntimeException("unknown direction for agent in PullBlock task");
//        }

        boolean agentInRoom = CleanupState.regionContainsPoint(room, ax, ay, false);
        if (!agentInRoom) { return false; }

        return true;
    }

}