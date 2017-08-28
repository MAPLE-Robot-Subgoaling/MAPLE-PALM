package cleanup.hierarchies;

import burlap.mdp.core.oo.state.ObjectInstance;
import cleanup.Cleanup;
import cleanup.state.CleanupDoor;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cleanup.Cleanup.ATT_CONNECTED;
import static cleanup.Cleanup.ATT_LOCKED;
import static cleanup.Cleanup.CLASS_DOOR;

public class MoveDoor extends CleanupDoor {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_LOCKED,
            ATT_CONNECTED
    );

    public MoveDoor(String name, String lockedStatus, HashSet<String> connected) {
        this.set(ATT_LOCKED, lockedStatus);
        this.set(ATT_CONNECTED, connected);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_DOOR;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new MoveDoor(objectName, (String) get(ATT_LOCKED), (HashSet<String>) get(ATT_CONNECTED));
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    public void addConnectedRegion(String name) {
        Set<String> connected = (Set<String>) get(ATT_CONNECTED);
        connected.add(name);
    }

}
