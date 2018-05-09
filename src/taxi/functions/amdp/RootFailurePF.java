package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class RootFailurePF extends PropositionalFunction {

    public RootFailurePF() {
        super("rootFail", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        // impossible to fail this domain
        return false;
    }

}
