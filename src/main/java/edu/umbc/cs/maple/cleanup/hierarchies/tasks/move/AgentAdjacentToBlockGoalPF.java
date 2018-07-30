package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.Cleanup;

import static edu.umbc.cs.maple.cleanup.Cleanup.CLASS_AGENT;
import static edu.umbc.cs.maple.cleanup.Cleanup.CLASS_BLOCK;

public class AgentAdjacentToBlockGoalPF extends PropositionalFunction {

    private static final String PF_AGENT_TO_BLOCK_GOAL = "agentAdjacentToBlock";

    public AgentAdjacentToBlockGoalPF() {
        super(PF_AGENT_TO_BLOCK_GOAL, new String[]{CLASS_BLOCK});
    }

    @Override
    public boolean isTrue(OOState ooState, String[] params) {
        return Cleanup.isAdjacent(ooState, params);
    }

}