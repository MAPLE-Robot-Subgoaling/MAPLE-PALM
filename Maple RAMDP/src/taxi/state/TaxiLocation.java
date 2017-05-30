package taxi.state;

import static taxi.TaxiDomain.VAR_LOCATION;
import static taxi.TaxiDomain.VAR_X;
import static taxi.TaxiDomain.VAR_Y;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;
import taxi.TaxiDomain;

/**
 * Created by ngopalan on 6/14/16.
 */
@DeepCopyState
public class TaxiLocation implements ObjectInstance {

    public int x;
    public int y;
    public String colour;

    public String name;
    private final static List<Object> keys = Arrays.<Object>asList(VAR_LOCATION, VAR_Y, VAR_X);
    
    public TaxiLocation() {
    	
    }
    
    public TaxiLocation(int x, int y, String name, String colour) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.colour = colour;
    }

    @Override
    public String className() {
        return TaxiDomain.LOCATIONCLASS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TaxiLocation copyWithName(String objectName) {
        return new TaxiLocation(this.x, this.y, objectName, this.colour);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("TaxiLocation variable key must be a string");
        }
        String key = (String)variableKey;
        if(key.equals(VAR_X)){
            return x;
        }
        else if(key.equals(VAR_Y)){
            return y;
        }
        else if(key.equals(VAR_LOCATION)){
            return colour;
        }

        throw new RuntimeException("Unknown key for TaxiLocation " + key);
    }

    @Override
    public TaxiLocation copy() {
        return new TaxiLocation(x,y,name,colour);
    }

    @Override
    public String toString() {
        return name+"(location), (" + x + ", " + y + "), colour: " + colour;
    }
}