package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import edu.umbc.cs.maple.cleanup.state.CleanupRoom;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;


public class MoveRoom extends CleanupRoom {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_CONNECTED,
            ATT_COLOR
    );

    public MoveRoom(String name, String color, Set<String> connected) {
        this.setName(name);
        this.set(ATT_COLOR, color);
        this.set(ATT_CONNECTED, connected);
    }

    @Override
    public String className() {
        return CLASS_ROOM;
    }

    @Override
    public MoveRoom copyWithName(String objectName) {
        return new MoveRoom(objectName, (String) get(ATT_COLOR), (Set<String>) this.get(ATT_CONNECTED));
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
