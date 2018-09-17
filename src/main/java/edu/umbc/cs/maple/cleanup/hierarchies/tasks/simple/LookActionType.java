package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.IntegerParameterizedActionType;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class LookActionType extends DirectionActionType {

    public LookActionType(String name, int parameterCount) {
        super(name, parameterCount);
    }

    public LookActionType(String name, String[] parameterOrderGroups) {
        super(name, parameterOrderGroups);
    }

    public LookActionType(){
        super(null,null);
    }


    @Override
    public List<Action> allApplicableActions(State state) {
        //System.out.println("ARRIVED AT APPL LOOK\n\t" + dx + " " + dy);
        List<Action> actions = new ArrayList<>();
        //get agent and block from state
        CleanupState cstate = (CleanupState) state;
        CleanupAgent agent = cstate.getAgent();

        //get agent coordinates and direction
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        String direction = (String) agent.get(ATT_DIR);
        //cannot look at current direction (face_x,face_y)
        int face_x = 0;
        int face_y = 0;
        if(direction.equals("north"))
            face_y = 1;
        else if(direction.equals("south"))
            face_y = -1;
        else if(direction.equals("east"))
            face_x = 1;
        else if(direction.equals("west"))
            face_x = -1;

        //array of coordinate around agent
            int[] lookSquare = new int[]{ax + super.dx,
                                         ay + super.dy};
            //check if agent is already facing
            //check if there is block
            //check if can push
            //if not, can look at coordinate
            if(!(super.dx == face_x && super.dy == face_y) &&
                 cstate.blockAt(lookSquare[0], lookSquare[1]) &&
                 super.allApplicableActions(state).size()<1) {

                        actions.add(createAction(lookSquare));
            }
            //System.out.println("\tRETURN IS: " + actions.size());
        return actions;
    }
}
