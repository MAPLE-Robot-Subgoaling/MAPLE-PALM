package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.state.OOState;
import cleanup.hierarchies.tasks.pick.ObjectToRegionActionType;

public class ObjectInRegionFailPF extends ObjectInRegionGoalPF {

    public ObjectInRegionFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        return !ObjectToRegionActionType.canMoveObjectToRegion(state, params);
    }

}
