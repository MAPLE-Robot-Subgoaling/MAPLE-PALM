package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.LCRootState;

public class RootFailurePF extends PropositionalFunction {

    public RootFailurePF() {
        super("rootFail", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        //cannot fail Root task, can only fail nav task,
        //todo: pass nav fail up through root
        return ((LCRootState)s).hasFailed;
    }

}
