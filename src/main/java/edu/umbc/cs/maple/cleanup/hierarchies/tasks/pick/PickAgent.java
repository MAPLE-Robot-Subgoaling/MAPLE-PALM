package edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick;

import edu.umbc.cs.maple.cleanup.hierarchies.tasks.move.MoveAgent;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_REGION;

public class PickAgent extends MoveAgent {

    public PickAgent(String name, String region) {
        super(name, region);
    }

    @Override
    public PickAgent copyWithName(String objectName) {
        return new PickAgent(objectName, (String) get(ATT_REGION));
    }


}
