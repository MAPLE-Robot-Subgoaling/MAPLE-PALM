package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.Arrays;

import static cleanup.Cleanup.ATT_CONNECTED;
//import static cleanup.Cleanup.ATT_REGION;
//
//public class MoveObjectDoorActionType extends ObjectToRegionActionType {
//    public MoveObjectDoorActionType(String name, String[] parameterClasses) {
//        super(name, parameterClasses);
//    }
//
//    @Override
//    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
//        boolean result = super.applicableInState(s, objectParameterizedAction);
//        // applicable if object NOT already in the region
//        if (!result) { return false; }
//
//        OOState state = (OOState) s;
//        String[] params = objectParameterizedAction.getObjectParameters();
//        String objectName = params[0];
//        String nextRegion = params[1];
//        ObjectInstance object = state.object(objectName);
//        String currentRegionName = (String) object.get(ATT_REGION);
//        ObjectInstance currentRegion = state.object(currentRegionName);
//        String[] connectedRegions = (String[]) currentRegion.get(ATT_CONNECTED);
//        // current region must be connected to next door/room
//        return Arrays.asList(connectedRegions).contains(nextRegion);
//    }
//}
