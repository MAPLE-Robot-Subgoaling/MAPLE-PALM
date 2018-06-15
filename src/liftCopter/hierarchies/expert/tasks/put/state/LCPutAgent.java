package liftCopter.hierarchies.expert.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.ATT_LOCATION;
import static taxi.TaxiConstants.CLASS_TAXI;

public class LCPutAgent extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_LOCATION
            );

    public LCPutAgent(String name, String location) {
        this(name, (Object)location);
    }

    private LCPutAgent(String name, Object location) {
        this.set(ATT_LOCATION, location);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_TAXI;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCPutAgent( objectName, get(ATT_LOCATION));
    }

    @Override
    public LCPutAgent copy() {
        return (LCPutAgent) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
