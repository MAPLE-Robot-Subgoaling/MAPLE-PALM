package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

import java.util.ArrayList;
import java.util.List;

public class ObjectToRegionActionType extends ObjectParameterizedActionType {

    public ObjectToRegionActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State state, ObjectParameterizedAction objectParameterizedAction) {
        return true;
    }

}
