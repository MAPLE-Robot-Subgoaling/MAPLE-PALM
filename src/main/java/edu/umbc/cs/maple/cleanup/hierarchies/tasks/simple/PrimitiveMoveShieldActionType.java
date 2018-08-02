package edu.umbc.cs.maple.cleanup.hierarchies.tasks.simple;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class PrimitiveMoveShieldActionType extends ObjectParameterizedActionType {

    public PrimitiveMoveShieldActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public PrimitiveMoveShieldActionType(String name, String[] parameterClasses, String[] parameterOrderGroups) {
        super(name, parameterClasses, parameterOrderGroups);
    }

    @Override
    protected boolean applicableInState(State state, ObjectParameterizedAction a) {
        CleanupState cstate = (CleanupState) state;

        CleanupAgent agent = cstate.getAgent();

        return false;
    }
}
