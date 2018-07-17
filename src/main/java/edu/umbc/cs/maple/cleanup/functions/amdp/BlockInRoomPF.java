package edu.umbc.cs.maple.cleanup.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.Cleanup.InRegion;

public class BlockInRoomPF extends InRegion {

    public BlockInRoomPF() {
        super(Cleanup.PF_BLOCK_IN_ROOM, new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_ROOM}, false);
    }

}
