package taxi.hierGen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class FailureFunction extends PropositionalFunction {

    public FailureFunction(){
        super("failure", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return false;
    }
}
