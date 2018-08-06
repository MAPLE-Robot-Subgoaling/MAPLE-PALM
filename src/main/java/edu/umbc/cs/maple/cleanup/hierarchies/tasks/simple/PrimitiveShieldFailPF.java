package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class PrimitiveShieldFailPF extends PropositionalFunction {

    public PrimitiveShieldFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public PrimitiveShieldFailPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }

    public PrimitiveShieldFailPF(){
        super(null,null,null);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return false;
    }
}
