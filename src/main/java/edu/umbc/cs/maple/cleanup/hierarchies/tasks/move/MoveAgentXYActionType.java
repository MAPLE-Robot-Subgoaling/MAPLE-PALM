package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class MoveAgentXYActionType implements ActionType {
    @Override
    public String typeName() {
        return "moveAgentXY";
    }

    @Override
    public Action associatedAction(String s) {
        String[] params = StringFormat.split(s);
        int goalX = Integer.parseInt(params[1]);
        int goalY = Integer.parseInt(params[2]);
        return new MoveAgentXYAction(goalX, goalY);
    }

    @Override
    public List<Action> allApplicableActions(State state) {
        //add all xy coordinates in rooms and doors.
        //exclude current xy position and walls
        List<Action> actions = new ArrayList<Action>();
        OOState oos = (OOState) state;

        CleanupState cstate = (CleanupState) state;
        CleanupAgent agent = cstate.getAgent();

        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);

        //add all room coordinates
        List<CleanupRoom> rooms = new ArrayList<>(cstate.getRooms().values());
        for(CleanupRoom room: rooms){
            int left = (int) room.get(Cleanup.ATT_LEFT);
            int right = (int) room.get(Cleanup.ATT_RIGHT);
            int bottom = (int) room.get(Cleanup.ATT_BOTTOM);
            int top = (int) room.get(Cleanup.ATT_TOP);
            for(int x=left+1;x<right;x++){
                for(int y=bottom+1;y<top;y++){
                    //do not include agent's current position
                    if(!(x==ax && y==ay)) {
                        actions.add(new MoveAgentXYAction(x, y));
                    }
                }
            }
        }
        //add all door coordinates
        List<CleanupDoor> doors = new ArrayList<>(cstate.getDoors().values());
        for(CleanupDoor door: doors){
            int left = (int) door.get(Cleanup.ATT_LEFT);
            int right = (int) door.get(Cleanup.ATT_RIGHT);
            int bottom = (int) door.get(Cleanup.ATT_BOTTOM);
            int top = (int) door.get(Cleanup.ATT_TOP);
            for(int x=left+1;x<right;x++){
                for(int y=bottom+1;y<top;y++){
                    //do not include agent's current position
                    if(!(x==ax && y==ay)) {
                        actions.add(new MoveAgentXYAction(x, y));
                    }
                }
            }
        }
        return actions;
    }
}
