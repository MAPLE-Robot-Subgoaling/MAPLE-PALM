package taxi.state;

import static taxi.TaxiDomain.VAR_GOALLOCATION;
import static taxi.TaxiDomain.VAR_INTAXI;
import static taxi.TaxiDomain.VAR_JUSTPICKEDUP;
import static taxi.TaxiDomain.VAR_ORIGINALSOURCELOCATION;
import static taxi.TaxiDomain.VAR_PICKEDUPATLEASTONCE;
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
public class TaxiPassenger implements ObjectInstance {

    public int x;
    public int y;
    public boolean inTaxi;
    public boolean justPickedUp;
    // in the original domain passenger needs to be picked up and dropped at least once for goal condition
    public boolean pickedUpAtLeastOnce;
    public String goalLocation;
    public String originalSourceLocation;


    public String name;



    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_JUSTPICKEDUP, VAR_INTAXI, VAR_GOALLOCATION, VAR_PICKEDUPATLEASTONCE ,VAR_ORIGINALSOURCELOCATION);

    public TaxiPassenger() {
    	
    }

    public TaxiPassenger(String name, int x, int y,  boolean inTaxi,
                         boolean justPickedUp, String goalLocation,
                         boolean pickedUpAtleastOnce, String originalSourceLocation) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.justPickedUp = justPickedUp;
        this.inTaxi = inTaxi;
        this.goalLocation = goalLocation;
        this.pickedUpAtLeastOnce = pickedUpAtleastOnce;
        this.originalSourceLocation = originalSourceLocation;
    }

    public TaxiPassenger(String name, int x, int y,  String goalLocation, String originalSourceLocation) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.justPickedUp = false;
        this.inTaxi = false;
        this.goalLocation = goalLocation;
        this.originalSourceLocation = originalSourceLocation;
    }


    @Override
    public String className() {
        return TaxiDomain.PASSENGERCLASS;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public TaxiPassenger  copyWithName(String objectName) {
        return new TaxiPassenger(objectName, x, y, inTaxi, justPickedUp,
                goalLocation, pickedUpAtLeastOnce, originalSourceLocation);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("TaxiPassenger variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(VAR_X)){
            return x;
        }
        else if(key.equals(VAR_Y)){
            return y;
        }
        else if(key.equals(VAR_GOALLOCATION)){
            return goalLocation;
        }
        else if(key.equals(VAR_INTAXI)){
            return inTaxi;
        }
        else if(key.equals(VAR_JUSTPICKEDUP)){
            return justPickedUp;
        }
        else if(key.equals(VAR_PICKEDUPATLEASTONCE)){
            return pickedUpAtLeastOnce;
        }
        else if(key.equals(VAR_ORIGINALSOURCELOCATION)){
            return originalSourceLocation;
        }

        throw new RuntimeException("Unknown key for TaxiPassenger " + key);
    }

    @Override
    public TaxiPassenger copy() {
        return new TaxiPassenger(name, x, y, inTaxi, justPickedUp,
                goalLocation, pickedUpAtLeastOnce, originalSourceLocation);
    }

    @Override
    public String toString() {
        String out = "";
        out += name+"(passenger), (" + x + ", " + y + ")";
        out += ", goalLocation: " + goalLocation;
        out += ", originalLocation: " + originalSourceLocation;
        out += ", justPickedUp: " + justPickedUp;
        out += ", pickedUpAtLeastOnce:" + pickedUpAtLeastOnce;
        return out;
    }
}