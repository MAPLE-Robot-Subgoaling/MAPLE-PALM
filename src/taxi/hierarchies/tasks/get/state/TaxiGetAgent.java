package taxi.hierarchies.tasks.get.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.ATT_LOCATION;
import static taxi.TaxiConstants.CLASS_TAXI;

public class TaxiGetAgent extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_LOCATION
            );

    public TaxiGetAgent(String name, String location) {
        this(name, (Object)location);
    }

    private TaxiGetAgent(String name, Object location) {
        this.set(ATT_LOCATION, location);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_TAXI;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiGetAgent( objectName, get(ATT_LOCATION));
    }

    @Override
    public TaxiGetAgent copy() {
        return (TaxiGetAgent) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
