package edu.umbc.cs.maple.cleanup.pfs;

import edu.umbc.cs.maple.cleanup.Cleanup;

public class BlockInRoomPF extends InRegion {

    public BlockInRoomPF() {
        super(Cleanup.PF_BLOCK_IN_ROOM, new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_ROOM}, false);
    }

}
