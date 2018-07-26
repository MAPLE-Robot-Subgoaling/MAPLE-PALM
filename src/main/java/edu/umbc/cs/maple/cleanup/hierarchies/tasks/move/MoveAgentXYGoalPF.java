package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;

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
        int x = Integer.parseInt(strings[1]);
        int y = Integer.parseInt(strings[2]);
        ObjectInstance agent = ooState.object(CLASS_AGENT);
        int ax = (int) agent.variableKeys().get(0);
        int ay = (int) agent.variableKeys().get(2);

        return ax==x && ay==y;
    }
}
