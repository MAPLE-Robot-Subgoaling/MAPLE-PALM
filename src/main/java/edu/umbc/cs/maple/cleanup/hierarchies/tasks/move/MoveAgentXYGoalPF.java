package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;
import static edu.umbc.cs.maple.cleanup.Cleanup.CLASS_AGENT;

public class MoveAgentXYGoalPF extends PropositionalFunction {
    public MoveAgentXYGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public MoveAgentXYGoalPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }

    public MoveAgentXYGoalPF(){
        super(null, null,null);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        String[] params = StringFormat.split(strings[0]);
        int x = Integer.parseInt(params[1]);
        int y = Integer.parseInt(params[2]);
        CleanupState cstate = (CleanupState) ooState;
        CleanupAgent agent = cstate.getAgent();
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);

        return ax==x && ay==y;
    }
}
