package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.state.OOState;

public class ObjectInRegionFailPF extends ObjectInRegionGoalPF {

    public ObjectInRegionFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        return false;
    }
//        String objectName = params[0];
//        String regionName = params[1];
//        ObjectInstance object = state.object(objectName);
//        boolean alreadyInRegion = super.isTrue(state, params);
//
//        // technically, this is the goal
//        if (alreadyInRegion) {
//            return false;
//        }
//
        // determine what rooms or doors this connects to (only goes room->door->room or door->room)
//        HashSet<String> connectedRegions = getConnectedRegions((CleanupState) state, regionName);
//        CleanupState cs = ((CleanupState) state);
//        if (!objectName.contains("agent")) {
//            CleanupAgent agent = cs.getAgent();
//            ObjectInstance agentRegion = cs.getContainingDoorOrRoom(agent);
//            ObjectInstance objectRegion = cs.getContainingDoorOrRoom(object);
//            String agentRegionName = agentRegion.name();
//            String objectRegionName = objectRegion.name();
//            HashSet<String> connectedRegions = getConnectedRegions((CleanupState) state, agentRegionName);
//            if (!agentRegionName.equals(objectRegionName) && !connectedRegions.contains(objectRegionName)) {
//                // fail if agent not in room or connected room to object
//                return true;
//            }
//
//        }
//        return false;
//        for (String connectedRegionName : connectedRegions) {
//            if (super.isTrue(state, new String[]{objectName, connectedRegionName})) {
//                // object is in a connected region, so this does not fail
//                return false;
//            }
//        }
//        // NOT in the current region, or in a connected region, then it fails
//        return true;
//    }

//        for ()
//        if (!inConnectedRegion) {
//            return true;
//        }
//
//        if (!objectName.contains("agent")) {
//            String agentName = ((CleanupState)state).getAgent().name();
//            if (super.isTrue(state, new String[]{agentName, regionName})) {
//                // agent is in a connected region
//                return false;
//            }
//            for (String connectedRegionName : connectedRegions) {
//                if (super.isTrue(state, new String[]{agentName, connectedRegionName})) {
//                    // agent is in a connected region
//                    return false;
//                }
//            }
//        }
//
//    }
}
