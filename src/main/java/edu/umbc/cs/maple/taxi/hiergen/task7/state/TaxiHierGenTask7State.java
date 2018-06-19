package edu.umbc.cs.maple.taxi.hiergen.task7.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.taxi.hiergen.TaxiHierGenState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiHierGenTask7State extends TaxiHierGenState implements MutableOOState, DeepCopyForShallowCopyState {

    private TaxiHierGenTask7Taxi taxi;
    private Map<String, TaxiHierGenTask7Passenger> passengers;

    public TaxiHierGenTask7State(TaxiHierGenTask7Taxi taxi, List<TaxiHierGenTask7Passenger> passes){
        this.taxi = taxi;

        this.passengers = new HashMap<String, TaxiHierGenTask7Passenger>();
        for(TaxiHierGenTask7Passenger passenger : passes){
            this.passengers.put(passenger.name(), passenger);
        }
    }

    private TaxiHierGenTask7State(TaxiHierGenTask7Taxi taxi, Map<String, TaxiHierGenTask7Passenger> passes){
        this.taxi = taxi;
        this.passengers = passes;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Not needed for HierGen");
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiHierGenTask7Taxi) {
            touchTaxi();
            taxi = null;
        } else if (objectInstance instanceof TaxiHierGenTask7Passenger) {
            touchPassenger(oname);
            passengers.remove(oname);
        } else {
            throw new RuntimeException("Error: unknown object of name: " + oname);
        }
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Not needed for HierGen");
    }

    @Override
    public int numObjects() {
        int total = taxi == null ? 0 : 1;
        total += passengers.size();
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(taxi.name().equals(oname))
            return taxi;
        return passengers.get(oname);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> objects = new ArrayList<ObjectInstance>(passengers.values());
        if (taxi != null) objects.add(taxi);
        return objects;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_TAXI))
            return Arrays.<ObjectInstance>asList(taxi);
        else if(oclass.equals(CLASS_PASSENGER))
            return new ArrayList<>(passengers.values());
        throw new RuntimeException("No object class " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        return null;
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        return OOStateUtilities.get(this, variableKey);
    }

    @Override
    public TaxiHierGenTask7State copy() {
        return new TaxiHierGenTask7State(taxi, passengers);
    }

//    @Override
//    public int getLocationX(String pname) {
//        return (int) passengers.get(pname).get(ATT_X);
//    }
//
//    @Override
//    public int getLocationY(String pname) {
//        return (int) passengers.get(pname).get(ATT_Y);
//    }

    //get values from objects
    public String[] getPassengers(){
        String[] ret = new String[passengers.size()];
        int i = 0;
        for(String name : passengers.keySet())
            ret[i++] = name;
        return ret;
    }

//    @Override
//    public String getPassengerLocation(String pname) {
//        boolean inTaxi = (boolean) passengers.get(pname).get(ATT_IN_TAXI);
//        int tx = (int) taxi.get(ATT_X);
//        int ty = (int) taxi.get(ATT_Y);
//        int px = (int) passengers.get(pname).get(ATT_X);
//        int py = (int) passengers.get(pname).get(ATT_Y);
//
//        if(inTaxi)
//            return ATT_IN_TAXI;
//        else if(tx == px && ty == py)
//            return ATT_;
//        else
//            return ATT_VAL_ON_ROAD;
//
//    }

    public TaxiHierGenTask7Taxi touchTaxi() {
        if (this.taxi != null) { this.taxi = taxi.copy(); }
        return taxi;
    }

    public TaxiHierGenTask7Passenger touchPassenger(String name){
        TaxiHierGenTask7Passenger p = passengers.get(name).copy();
        touchPassengers().remove(name);
        passengers.put(name, p);
        return p;
    }

    public Map<String, TaxiHierGenTask7Passenger> touchPassengers(){
        this.passengers = new HashMap<>(passengers);
        return passengers;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiHierGenTask7State copy = this.copy();
        copy.touchTaxi();
        copy.touchPassengers();
        return copy;
    }

    @Override
    public ObjectInstance getTaxi() {
        return taxi;
    }
}
