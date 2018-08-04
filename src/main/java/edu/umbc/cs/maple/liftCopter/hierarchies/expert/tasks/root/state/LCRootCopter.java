package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCRootCopter extends MutableObject {
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_LOCATION
    );

    public LCRootCopter(String name, String currentLocation) {
        this(name, (Object) currentLocation);
    }

    private LCRootCopter(String name, Object currentLocation){
        this.set(ATT_LOCATION, currentLocation);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_AGENT;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCRootCopter(
                objectName,
                get(ATT_LOCATION)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }
    @Override
    public LCRootCopter copy() {
        return (LCRootCopter) copyWithName(name());
    }
}
