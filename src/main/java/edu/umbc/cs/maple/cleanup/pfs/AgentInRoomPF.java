package edu.umbc.cs.maple.cleanup.pfs;

        import edu.umbc.cs.maple.cleanup.Cleanup;

public class AgentInRoomPF extends InRegion {

    public AgentInRoomPF() {
        super(Cleanup.PF_AGENT_IN_ROOM, new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_ROOM}, false);
    }

}
