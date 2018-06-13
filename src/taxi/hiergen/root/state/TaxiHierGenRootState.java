package taxi.hiergen.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import taxi.hiergen.TaxiHierGenState;
import utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static taxi.TaxiConstants.*;

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

//
//    public String getPassengerLocation(String pname) {
//        boolean inTaxi = (boolean) passengers.get(pname).get(ATT_IN_TAXI);
//        int tx = (int) taxi.get(ATT_X);
//        int ty = (int) taxi.get(ATT_Y);
//        int px = (int) passengers.get(pname).get(ATT_X);
//        int py = (int) passengers.get(pname).get(ATT_Y);
//
//        if(!inTaxi)
//            return ATT_VAL_NOT_IN_TAXI;
//        else if(tx == px && ty == py)
//            return ATT_VAL_IN_TAXI;
//        else
//            return ATT_VAL_ON_ROAD;
//    }

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
}
