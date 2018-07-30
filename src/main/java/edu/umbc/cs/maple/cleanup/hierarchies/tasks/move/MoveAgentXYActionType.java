package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
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
        List<ObjectInstance> rooms = oos.objectsOfClass(CLASS_ROOM);
        ObjectInstance agent = oos.object(CLASS_AGENT);
        int ax = (int) agent.variableKeys().get(0);
        int ay = (int) agent.variableKeys().get(1);

        //add all room coordinates
        for(ObjectInstance room: rooms){
            int left = (int) room.variableKeys().get(0);
            int right = (int) room.variableKeys().get(1);
            int bottom = (int) room.variableKeys().get(2);
            int top = (int) room.variableKeys().get(3);
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
        List<ObjectInstance> doors = oos.objectsOfClass(CLASS_DOOR);
        for(ObjectInstance door: doors){
            int left = (int) door.variableKeys().get(0);
            int right = (int) door.variableKeys().get(1);
            int bottom = (int) door.variableKeys().get(2);
            int top = (int) door.variableKeys().get(3);
            for(int x=left;x<right;x++){
                for(int y=bottom;y<top;y++){
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
