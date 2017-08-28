package cleanup.hierarchies;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

public class PickRoomForObjectActionType extends ObjectParameterizedActionType {

    public PickRoomForObjectActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State state, ObjectParameterizedAction objectParameterizedAction) {
        return true;
    }

}
