package cleanup.hierarchies.tasks.root;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class CleanupRootFailPF extends PropositionalFunction {

    public CleanupRootFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return false;
    }
}