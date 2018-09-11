package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_DIR;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class LookGoalPF extends PropositionalFunction {
    public LookGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public LookGoalPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }

    public LookGoalPF(){
        super(null,null,null);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        if(strings == null)
            return false;
        String[] params = StringFormat.split(strings[0]);
        int lx = Integer.parseInt(params[1]);
        int ly = Integer.parseInt(params[2]);
        CleanupState cstate = (CleanupState) ooState;
        CleanupAgent agent = cstate.getAgent();
        if(agent == null){
            return false;
        }
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

        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);

        return (ax + dx == lx) && (ay + dy == ly);
    }
}
