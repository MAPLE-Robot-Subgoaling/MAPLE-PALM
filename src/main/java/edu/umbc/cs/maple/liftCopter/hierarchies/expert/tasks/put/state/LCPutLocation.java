package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.CLASS_LOCATION;


public class LCPutLocation extends MutableObject{

    private final static List<Object> keys = Arrays.<Object>asList( );

    public LCPutLocation(String name) {
        this.setName(name);;
    }

    @Override
    public String className() {
        return CLASS_LOCATION;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCPutLocation( objectName);
    }

    @Override
    public LCPutLocation copy() {
        return (LCPutLocation) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
