package edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_REGION;

public class PickObjectRoomGoalPF extends PropositionalFunction {

    public PickObjectRoomGoalPF(){
        super("pick", new String[]{});
    }
    public PickObjectRoomGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        String objectName = params[0];
        String regionName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance region = state.object(regionName);
        if (object == null || region == null) { return false; }
        return object.get(ATT_REGION).equals(region.name());
    }
}