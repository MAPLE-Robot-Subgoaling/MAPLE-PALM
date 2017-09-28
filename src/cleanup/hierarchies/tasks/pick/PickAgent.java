package cleanup.hierarchies.tasks.pick;

import cleanup.Cleanup;
import cleanup.hierarchies.tasks.move.MoveAgent;
import cleanup.state.CleanupAgent;

import java.util.Arrays;
import java.util.List;

import static cleanup.Cleanup.ATT_REGION;

public class PickAgent extends MoveAgent {


    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_REGION
    );

    public PickAgent() {

    }

    public PickAgent(String name, String region) {
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
    public PickAgent copy() {
        return this.copyWithName(this.name());
    }

    @Override
    public PickAgent copyWithName(String objectName) {
        return new PickAgent(objectName, (String) get(ATT_REGION));
    }

}
