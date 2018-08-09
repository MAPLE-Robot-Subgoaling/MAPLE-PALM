package edu.umbc.cs.maple.cleanup.pfs;

import edu.umbc.cs.maple.cleanup.Cleanup;

public class AgentInDoorPF extends InRegion {

    public AgentInDoorPF() {
        super(Cleanup.PF_AGENT_IN_DOOR, new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_DOOR}, true);
    }

}
