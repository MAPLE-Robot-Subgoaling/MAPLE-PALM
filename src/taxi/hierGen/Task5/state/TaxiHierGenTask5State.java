package taxi.hierGen.Task5.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import taxi.hierarchies.tasks.put.state.TaxiPutAgent;
import utilities.DeepCopyForShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaxiHierGenTask5State implements MutableOOState, DeepCopyForShallowCopyState {

    public static final String CLASS_ROOT_Taxi =			"tas5Taxi";
    public static final String ACTION_Task5_Action = 		"task5Action";

    private TaxiHierGenTask5Taxi taxi;

    public TaxiHierGenTask5State(TaxiHierGenTask5Taxi taxi){
        this.taxi = taxi;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Not needed for HierGen");
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiPutAgent) {
            touchTaxi();
            taxi = null;
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
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(taxi.name().equals(oname))
            return taxi;
        throw new RuntimeException("No object with name " + oname);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
        if (taxi != null) objects.add(taxi);
        return objects;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_ROOT_Taxi))
            return Arrays.<ObjectInstance>asList(taxi);
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
    public TaxiHierGenTask5State copy() {
        return new TaxiHierGenTask5State(taxi);
    }

    //get values from objects
    public Object getTaxiAtt(String attName){
        return taxi.get(attName);
    }

    public String getTaxiName(){
        return taxi.name();
    }

    public TaxiHierGenTask5Taxi touchTaxi() {
        if (this.taxi != null) { this.taxi = taxi.copy(); }
        return taxi;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiHierGenTask5State copy = this.copy();
        copy.touchTaxi();
        return copy;
    }

}
