package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class LookFailPF extends PropositionalFunction {
    public LookFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public LookFailPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }

    public LookFailPF(){
        super(null,null,null);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return false;
    }
}
