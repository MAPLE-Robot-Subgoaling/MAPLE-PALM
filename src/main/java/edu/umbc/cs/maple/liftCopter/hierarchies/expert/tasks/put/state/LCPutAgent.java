package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;


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
        return CLASS_AGENT;
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
