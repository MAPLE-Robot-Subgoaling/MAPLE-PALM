package edu.umbc.cs.maple.cleanup.pfs;

import edu.umbc.cs.maple.cleanup.Cleanup;

public class BlockInDoorPF extends InRegion {

    public BlockInDoorPF() {
        super(Cleanup.PF_BLOCK_IN_DOOR, new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_DOOR}, true);
    }

}
