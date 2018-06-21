package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.state.annotations.DeepCopyState;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_REGION;

@DeepCopyState
public class MoveAgent extends CleanupAgent {


    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_REGION
    );

    public MoveAgent() {

    }

    public MoveAgent(String name, String region) {
        this.setName(name);
        this.set(ATT_REGION, region);
    }

    @Override
    public String className() {
        return Cleanup.CLASS_AGENT;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public MoveAgent copy() {
        return this.copyWithName(this.name());
    }

    @Override
    public MoveAgent copyWithName(String objectName) {
        return new MoveAgent(objectName, (String) get(ATT_REGION));
    }

}
