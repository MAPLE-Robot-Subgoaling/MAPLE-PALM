package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.IntegerParameterizedActionType;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class PullActionType extends IntegerParameterizedActionType {
    public PullActionType(String name, int parameterCount) {
        super(name, parameterCount);
    }

    public PullActionType(String name, String[] parameterOrderGroups) {
        super(name, parameterOrderGroups);
    }

    public PullActionType(){
        super(null,null);
    }

    @Override
    public List<Action> allApplicableActions(State state) {
        List<Action> actions = new ArrayList<>();
        CleanupState cstate = (CleanupState) state;
        CleanupAgent agent = cstate.getAgent();
        //get agent coordinate and which direction it is facing
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        String direction = (String) agent.get(ATT_DIR);
        int dx = 0;
        int dy = 0;
        if(direction.equals("north"))
            dy = 1;
        else if(direction.equals("south"))
            dy = -1;
        else if(direction.equals("east"))
            dx = 1;
        else if(direction.equals("west"))
            dx = -1;
        else
            return actions;
        //if block one over in direction, true
        int nextx = ax + dx;
        int nexty = ay + dy;
        int[] next = {nextx, nexty};
        if(cstate.blockAt(nextx, nexty)) {
            actions.add(createAction(next));
            return actions;
        }
        //else, return empty list
        return actions;

    }
}
