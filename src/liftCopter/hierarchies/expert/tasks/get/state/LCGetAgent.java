package liftCopter.hierarchies.expert.tasks.get.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;


public class LCGetAgent extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_LOCATION
            );

    public LCGetAgent(String name, String location) {
        this(name, (Object)location);
    }

    private LCGetAgent(String name, Object location) {
        this.set(ATT_LOCATION, location);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_AGENT;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCGetAgent( objectName, get(ATT_LOCATION));
    }

    @Override
    public LCGetAgent copy() {
        return (LCGetAgent) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
