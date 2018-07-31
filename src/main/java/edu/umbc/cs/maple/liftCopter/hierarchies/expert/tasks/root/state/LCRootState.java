package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCRootState implements MutableOOState, DeepCopyForShallowCopyState {
    public boolean hasFailed = false;
    //this state has cargos
    private Map<String, LCRootCargo> cargos;

    public LCRootState(List<LCRootCargo> pass) {
        this.cargos = new HashMap<>();
        for(LCRootCargo p : pass){
            this.cargos.put(p.name(), p);
        }
    }

    private LCRootState(Map<String, LCRootCargo> pass) {
        this.cargos = pass;
    }

    @Override
    public int numObjects() {
        return cargos.size();
    }

    @Override
    public ObjectInstance object(String oname) {
        ObjectInstance o = cargos.get(oname);
        if(o != null)
            return o;

        return null;
    }

    private List<ObjectInstance> cachedObjectList = null;
    @Override
    public List<ObjectInstance> objects() {
        if (cachedObjectList == null) { cachedObjectList = new ArrayList<ObjectInstance>(); }
        else { return cachedObjectList; }
        List<ObjectInstance> obj = new ArrayList<>();
        obj.addAll(cargos.values());
        cachedObjectList = obj;
        return obj;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_CARGO))
            return new ArrayList<ObjectInstance>(cargos.values());
        throw new RuntimeException("No object class " + oclass);
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
    public LCRootState copy() {
        return new LCRootState(touchCargos());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(cargos.get(key.obName) != null){
            touchCargo(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof LCRootCargo || o.className().equals(CLASS_CARGO)){
            touchCargos().put(o.name(), (LCRootCargo) o);
        }else{
            throw new RuntimeException("Can only add certain objects to state.");
        }
        cachedObjectList = null;
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        touchCargo(oname);
        cargos.remove(oname);
        cachedObjectList = null;

        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    //touch methods allow a shallow copy of states and a copy of objects only when modified
    public LCRootCargo touchCargo(String passName){
        LCRootCargo p = cargos.get(passName).copy();
        touchCargos().remove(passName);
        cargos.put(passName, p);
        return p;
    }

    public Map<String, LCRootCargo> touchCargos(){
        this.cargos = new HashMap<>(cargos);
        return cargos;
    }

    //get values from objects
    public String[] getCargos(){
        String[] ret = new String[cargos.size()];
        int i = 0;
        for(String name : cargos.keySet())
            ret[i++] = name;
        return ret;
    }

    public Object getCargoAtt(String passname, String attName){
        return cargos.get(passname).get(attName);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("");
        for (LCRootCargo cargo : cargos.values()) {
            buf.append("C");
            buf.append(cargo.name().charAt(cargo.name().length()-1));
            buf.append(", at:");
            String at = (String) cargo.get(ATT_LOCATION);
            if (at.contains("Location")) {
                buf.append("L");
                buf.append(at.charAt(at.length()-1));
            } else {
                buf.append(at);
            }
            buf.append(", goal:");
            String goal = (String) cargo.get(ATT_GOAL_LOCATION);
            if (goal.contains("Location")) {
                buf.append("L");
                buf.append(goal.charAt(goal.length()-1));
            } else {
                buf.append(goal);
            }
            buf.append("; ");
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LCRootState that = (LCRootState) o;

        return cargos != null ? cargos.equals(that.cargos) : that.cargos == null;
    }

    @Override
    public int hashCode() {
        return cargos != null ? cargos.hashCode() : 0;
    }


    @Override
    public MutableOOState deepCopy() {
        LCRootState copy = this.copy();
        copy.touchCargos();
        return copy;
    }
}
