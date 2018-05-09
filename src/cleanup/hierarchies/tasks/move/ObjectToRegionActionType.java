package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

import java.util.HashSet;

import static cleanup.Cleanup.ATT_CONNECTED;
import static cleanup.Cleanup.ATT_REGION;

public class ObjectToRegionActionType extends ObjectParameterizedActionType {

    public ObjectToRegionActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        OOState state = (OOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String objectName = params[0];
        String regionName = params[1];
        ObjectInstance object = state.object(objectName);
        String currentRegionName = (String) object.get(ATT_REGION);
        boolean alreadyInRegion = currentRegionName.equals(regionName);
        // not applicable if object already in the region
        if (alreadyInRegion) { return false; }
        ObjectInstance currentRegion = state.object(currentRegionName);
        HashSet<String> connectedRegions = (HashSet<String>) currentRegion.get(ATT_CONNECTED);
        // current region must be connected to next door/room
        return connectedRegions.contains(regionName);
    }

}
