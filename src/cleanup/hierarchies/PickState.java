package cleanup.hierarchies;

import burlap.mdp.core.state.State;
import cleanup.state.CleanupBlock;
import cleanup.state.CleanupDoor;
import cleanup.state.CleanupRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickState extends MoveState {

    public PickState(PickAgent agent, List<PickBlock> blocks, List<PickRoom> rooms) {
        super(agent, blocks, new ArrayList<>(), rooms);
    }

}
