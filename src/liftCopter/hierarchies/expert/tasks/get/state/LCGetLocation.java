package liftCopter.hierarchies.expert.tasks.get.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static liftCopter.LiftCopterConstants.CLASS_LOCATION;

public class LCGetLocation extends MutableObject{

    private final static List<Object> keys = Arrays.<Object>asList( );

    public LCGetLocation(String name) {
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_LOCATION;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCGetLocation( objectName);
    }

    @Override
    public LCGetLocation copy() {
        return (LCGetLocation) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
