package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class PrimitiveShieldGoalPF extends PropositionalFunction {
    public PrimitiveShieldGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public PrimitiveShieldGoalPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }

    public PrimitiveShieldGoalPF(){
        super(null,null,null);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        String[] params = StringFormat.split(strings[0]);
        int x = Integer.parseInt(params[1]);
        int y = Integer.parseInt(params[2]);
        CleanupState cstate = (CleanupState) ooState;
        CleanupAgent agent = cstate.getAgent();
        if(agent==null){
            return false;
        }
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);

        return ax==x && ay==y;
    }
}
