package edu.umbc.cs.maple.taxi.hiergen.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.taxi.hiergen.TaxiHierGenState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiHierGenRootState extends TaxiHierGenState implements MutableOOState, DeepCopyForShallowCopyState {

    private TaxiHierGenRootTaxi taxi;
    private Map<String, TaxiHierGenRootPassenger> passengers;

    public TaxiHierGenRootState(TaxiHierGenRootTaxi taxi, List<TaxiHierGenRootPassenger> passes){
        this.taxi = taxi;

        this.passengers = new HashMap<>();
        for(TaxiHierGenRootPassenger passenger : passes){
            this.passengers.put(passenger.name(), passenger);
        }
    }

    private TaxiHierGenRootState(TaxiHierGenRootTaxi taxi, Map<String, TaxiHierGenRootPassenger> passes){
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
        if (objectInstance instanceof TaxiHierGenRootTaxi) {
            touchTaxi();
            taxi = null;
        } else if (objectInstance instanceof TaxiHierGenRootPassenger) {
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
        if (taxi == null) {
            return null;
        }
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
            return Arrays.asList(taxi);
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
    public TaxiHierGenRootState copy() {
        return new TaxiHierGenRootState(taxi, passengers);
    }

    public TaxiHierGenRootTaxi touchTaxi() {
        if (this.taxi != null) { this.taxi = taxi.copy(); }
        return taxi;
    }

    public TaxiHierGenRootPassenger touchPassenger(String name){
        TaxiHierGenRootPassenger p = passengers.get(name).copy();
        touchPassengers().remove(name);
        passengers.put(name, p);
        return p;
    }

    public Map<String, TaxiHierGenRootPassenger> touchPassengers(){
        this.passengers = new HashMap<>(passengers);
        return passengers;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiHierGenRootState copy = this.copy();
        copy.touchTaxi();
        copy.touchPassengers();
        return copy;
    }

    public TaxiHierGenRootTaxi getTaxi() {
        return taxi;
    }


    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("");
        for (ObjectInstance passenger : passengers.values()) {
            buf.append("P");
            buf.append(passenger.name().charAt(passenger.name().length()-1));
            buf.append(", at:");
            int atX = (int) passenger.get(ATT_X);
            int atY = (int) passenger.get(ATT_Y);
            buf.append("(");
            buf.append(atX);
            buf.append(",");
            buf.append(atY);
            buf.append(")");
            buf.append(", goal:");
            int goalX = (int) passenger.get(ATT_DESTINATION_X);
            int goalY = (int) passenger.get(ATT_DESTINATION_X);
            buf.append("(");
            buf.append(goalX);
            buf.append(",");
            buf.append(goalY);
            buf.append(")");
            buf.append(", inTaxi:");
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
            buf.append(inTaxi);
            buf.append("; ");
        }
        return buf.toString();
    }
}
