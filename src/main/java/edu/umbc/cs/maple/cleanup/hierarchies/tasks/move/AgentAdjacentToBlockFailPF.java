package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.Cleanup;

import static edu.umbc.cs.maple.cleanup.Cleanup.CLASS_BLOCK;

public class AgentAdjacentToBlockFailPF extends PropositionalFunction {

    private static final String PF_AGENT_TO_BLOCK_FAIL = "agentAdjacentToBlockFailPF";

    public AgentAdjacentToBlockFailPF() {
        super(PF_AGENT_TO_BLOCK_FAIL, new String[]{CLASS_BLOCK});
    }

    @Override
    public boolean isTrue(OOState ooState, String[] params) {
        return false;
    }

}