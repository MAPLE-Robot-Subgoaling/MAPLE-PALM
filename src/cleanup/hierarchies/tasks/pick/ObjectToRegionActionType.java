package cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import cleanup.state.CleanupAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static cleanup.Cleanup.ATT_CONNECTED;
import static cleanup.Cleanup.ATT_REGION;
import static cleanup.Cleanup.CLASS_AGENT;

public class ObjectToRegionActionType extends ObjectParameterizedActionType {

    public ObjectToRegionActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public static boolean canMoveObjectToRegion(State s, String[] params) {
        OOState state = (OOState) s;

        String objectName = params[0];
        String regionName = params[1];

        ObjectInstance object = state.object(objectName);
        ObjectInstance agent = state.objectsOfClass(CLASS_AGENT).get(0);
        String objectRegionName = (String) object.get(ATT_REGION);
        String agentRegionName = (String) agent.get(ATT_REGION);
        ObjectInstance objectRegion = state.object(objectRegionName);
        ObjectInstance agentRegion = state.object(agentRegionName);

        // not applicable if object already in the region
        boolean objectAlreadyInRegion = objectRegionName.equals(regionName);
        if (objectAlreadyInRegion) { return false; }

        // otherwise, object must be the agent or in a connecting room to the agent
        HashSet<String> connectedToAgentRegion = (HashSet<String>) agentRegion.get(ATT_CONNECTED);
        if (object.equals(agent) || connectedToAgentRegion.contains(objectRegion)) {
            // and the destination region must be connected to the agent or to the object
            HashSet<String> connectedToObjectRegion = (HashSet<String>) objectRegion.get(ATT_CONNECTED);
            if (connectedToAgentRegion.contains(regionName) || connectedToObjectRegion.contains(regionName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        String[] params = objectParameterizedAction.getObjectParameters();
        return canMoveObjectToRegion(s, params);
    }

}
