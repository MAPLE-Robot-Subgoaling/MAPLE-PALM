package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;


import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.move.MoveBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.MaskedParameterizedStateMapping;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveMapper implements MaskedParameterizedStateMapping {

    public static String moveBlockTargetAlias = "target";
    public static String moveBlockOtherAlias = "other";

    //the parameter should be the name of the object to be moved
    @Override
    public State mapState(State s, String... params) {
        CleanupState cs = (CleanupState) s;
        List<CleanupBlock> blocks = new ArrayList<>();
        String target = params[0];
        for(CleanupBlock block : cs.getBlocks().values()){
                blocks.add(block.copyWithName(block.name().equals(target) ? moveBlockTargetAlias : moveBlockOtherAlias));
        }
        return new CleanupState(cs.getWidth(), cs.getHeight(), cs.getAgent(),
                toNameMap(blocks),
                toNameMap(new ArrayList<>(cs.getRooms().values())),
                toNameMap(new ArrayList<>(cs.getDoors().values())));
    }
    public static <T extends MutableObject, F extends T> Map<String, T> toSuperMap(List<F> objects) {
        return toNameMap(new ArrayList<>(objects));
    }

    public static <T extends MutableObject> Map<String, T> toNameMap(List<T> objects){
        Map<String, T> map = new HashMap<>();
        for (T object : objects) {
            map.put(object.name(), object);
        }
        return map;
    }

    @Override
    public String[] getMaskedParameters() {
        return new String[]{Cleanup.CLASS_BLOCK};
    }
}
