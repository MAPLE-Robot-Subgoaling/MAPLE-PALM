package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import edu.umbc.cs.maple.cleanup.state.CleanupDoor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

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
    public MoveDoor copyWithName(String objectName) {
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
