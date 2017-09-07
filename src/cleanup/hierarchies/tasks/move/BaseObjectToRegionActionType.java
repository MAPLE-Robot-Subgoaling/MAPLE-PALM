package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import cleanup.state.CleanupDoor;
import cleanup.state.CleanupRoom;
import cleanup.state.CleanupState;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static cleanup.Cleanup.*;
import static cleanup.Cleanup.ATT_BOTTOM;
import static cleanup.Cleanup.ATT_TOP;
import static cleanup.hierarchies.tasks.pick.PickRoomAgentMapper.rectanglesIntersect;


public class BaseObjectToRegionActionType extends ObjectParameterizedActionType {

    private ObjectInRegionGoalPF inRegionPF;

    public BaseObjectToRegionActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
        inRegionPF = new ObjectInRegionGoalPF("internalPF_"+name, parameterClasses);
    }

    private HashSet<String> getConnectedRegions(CleanupState state, String regionName) {

        HashSet<String> connected = new HashSet<>();
        Collection<CleanupRoom> rooms = state.getRooms().values();
        Collection<CleanupDoor> doors = state.getDoors().values();

        for(CleanupRoom r : rooms){

            if (r.name().equals(regionName)) {

                int rl = (int) r.get(ATT_LEFT);
                int rr = (int) r.get(ATT_RIGHT);
                int rt = (int) r.get(ATT_TOP);
                int rb = (int) r.get(ATT_BOTTOM);

                for(CleanupDoor d : doors){

                    int dl = (int) d.get(ATT_LEFT);
                    int dr = (int) d.get(ATT_RIGHT);
                    int dt = (int) d.get(ATT_TOP);
                    int db = (int) d.get(ATT_BOTTOM);

                    if(rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr)){
                        connected.add(d.name());
                    }

                }

                return connected;
            }

        }

        for (CleanupDoor d : doors) {

            if (d.name().equals(regionName)) {

                int dl = (int) d.get(ATT_LEFT);
                int dr = (int) d.get(ATT_RIGHT);
                int dt = (int) d.get(ATT_TOP);
                int db = (int) d.get(ATT_BOTTOM);

                for (CleanupRoom r : rooms) {

                    int rl = (int) r.get(ATT_LEFT);
                    int rr = (int) r.get(ATT_RIGHT);
                    int rt = (int) r.get(ATT_TOP);
                    int rb = (int) r.get(ATT_BOTTOM);

                    if(rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr)){
                        connected.add(r.name());
                    }

                }

                return connected;
            }
        }

        return null;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        OOState state = (OOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String objectName = params[0];
        String regionName = params[1];
        ObjectInstance object = state.object(objectName);
        boolean alreadyInRegion = inRegionPF.isTrue(state, params);
        // not applicable if object already in the region
        if (alreadyInRegion) { return false; }

        // determine what rooms or doors this connects to (only goes room->door or door->room)
        HashSet<String> connectedRegions = getConnectedRegions((CleanupState) state, regionName);
        // current region must be connected to next door/room
        for (String connectedRegionName : connectedRegions) {
            if (inRegionPF.isTrue(state, new String[]{objectName, connectedRegionName})) {
                return true;
            }
        }
        return false;

    }

}

