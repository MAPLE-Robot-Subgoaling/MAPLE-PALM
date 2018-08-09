package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class ObjectInRoomFailPF extends PropositionalFunction {

    public ObjectInRoomFailPF(){
        super("objectInRoomFailPF", new String[]{});
    }

    public ObjectInRoomFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        return false;
    }

}