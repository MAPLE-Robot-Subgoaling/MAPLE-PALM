package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.IntegerParameterizedActionType;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class DirectionActionType extends IntegerParameterizedActionType {

    public int dx;
    public int dy;

    public DirectionActionType(String name, int parameterCount) {
        super(name, parameterCount);
    }

    public DirectionActionType(String name, String[] parameterOrderGroups) {
        super(name, parameterOrderGroups);
    }

    public DirectionActionType(){
        super(null,null);
    }

    public int[] getOneMore(int x, int y){
        int[] more = {x+dx, y+dy};
        return more;
    }

    @Override
    public List<Action> allApplicableActions(State state) {
        System.out.println("ARRIVED AT APPL DIRECTION\n\t\"" + dx + " " + dy);
        List<Action> actions = new ArrayList<>();
        CleanupState cstate = (CleanupState) state;
        CleanupAgent agent = cstate.getAgent();
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        //wall at xy?
        //if yes, cannot move
        int[] goTo = getOneMore(ax,ay);
        int nextx = goTo[0];
        int nexty = goTo[1];
        if(cstate.wallAt(nextx,nexty)){
            return new ArrayList<>();
        }
        //block at xy?
        if(cstate.blockAt(nextx, nexty)){
            //is there a wall or block (in the same direction) after it?
            //if yes, cannot move
            int[] afterBlock = getOneMore(goTo[0], goTo[1]);
            int twomorex = afterBlock[0];
            int twomorey = afterBlock[1];
            if(cstate.blockAt(twomorex, twomorey) || cstate.wallAt(twomorex, twomorey)){
                return new ArrayList<>();
            }
        }
        //else, can move!
        actions.add(createAction(goTo));
        System.out.println("\tRETURN IS: " + actions.size());
        return actions;
    }
}
