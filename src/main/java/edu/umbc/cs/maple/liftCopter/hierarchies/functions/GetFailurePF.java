package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state.LCGetState;
import edu.umbc.cs.maple.liftCopter.state.LiftCopterState;

import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class GetFailurePF extends PropositionalFunction {
    //get fails if any passenger if in taxi unless it is the right one

    public GetFailurePF() {
        super("getFail", new String[]{CLASS_CARGO});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        List<ObjectInstance> agents = s.objectsOfClass(CLASS_AGENT);
        if (agents.size() < 1) { return false; }
        ObjectInstance agent = agents.get(0);
        return agent.get(ATT_LOCATION).equals(ATT_VAL_CRASHED);
    }

}
