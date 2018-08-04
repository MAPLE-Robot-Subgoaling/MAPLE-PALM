package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCRootState implements MutableOOState, DeepCopyForShallowCopyState {
    public boolean hasFailed = false;
    //this state has cargos
    private Map<String, LCRootCargo> cargos;
    private LCRootCopter copter;

    public LCRootState(List<LCRootCargo> pass, LCRootCopter copter) {
        this.copter = copter;
        this.cargos = new HashMap<>();
        for(LCRootCargo p : pass){
            this.cargos.put(p.name(), p);
        }
    }

    private LCRootState(Map<String, LCRootCargo> pass, LCRootCopter copter) {
        this.copter = copter;
        this.cargos = pass;
    }

    @Override
    public int numObjects() {
        int total = 0;
        if (copter != null) { total += 1; }
        total += cargos.size();
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(copter != null && oname.equals(copter.name())) {
            return copter;
        }

        ObjectInstance o = cargos.get(oname);
        if(o != null) {
            return o;
        }

        return null;
    }

    private List<ObjectInstance> cachedObjectList = null;
    @Override
    public List<ObjectInstance> objects() {
        if (cachedObjectList == null) { cachedObjectList = new ArrayList<ObjectInstance>(); }
        else { return cachedObjectList; }
        List<ObjectInstance> obj = new ArrayList<>();
        obj.addAll(cargos.values());
        if(copter != null) obj.add(copter);
        cachedObjectList = obj;
        return obj;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_AGENT)) {
            return copter == null ? new ArrayList<>() : Arrays.<ObjectInstance>asList(copter);
        }
        if(oclass.equals(CLASS_CARGO)) {
            return new ArrayList<>(cargos.values());
        }
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
        return new LCRootState(touchCargos(), touchCopter());
    }


    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(copter != null && key.obName.equals(copter.name())) {
            touchCopter().set(variableKey, value);
        } else if(cargos.get(key.obName) != null){
            touchCargo(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof LCRootCopter || o.className().equals(CLASS_AGENT)) {
            copter = (LCRootCopter)o;
        } if(o instanceof LCRootCargo || o.className().equals(CLASS_CARGO)){
            touchCargos().put(o.name(), (LCRootCargo) o);
        }else{
            throw new RuntimeException("Can only add certain objects to state.");
        }
//        cachedObjectList = null;
        return this;
    }


    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof LCRootCopter) {
            touchCopter();
            copter = null;
        } else if (objectInstance instanceof LCRootCargo) {
            touchCargo(oname);
            cargos.remove(oname);
        } else {
            throw new RuntimeException("Error: unknown object of name: " + oname);
        }
//        cachedObjectList = null;
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
    public LCRootCopter touchCopter(){
        if (copter != null) { this.copter = copter.copy(); }
        return copter;
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

    public Object getCopterAtt( String attName){
        if(copter == null) return null;
        return copter.get(attName);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("");
        if (copter != null) {
            buf.append("LC:{");
            String at = (String) copter.get(ATT_LOCATION);
            if (at.contains("Location")) {
                buf.append("L");
                buf.append(at.charAt(at.length()-1));
            } else {
                buf.append(at);
            }
            buf.append("} ");
        }
        for (LCRootCargo cargo : cargos.values()) {
            buf.append("C");
            buf.append(cargo.name().charAt(cargo.name().length()-1));
            buf.append(":{");
            String at = (String) cargo.get(ATT_LOCATION);
            if (at.contains("Location")) {
                buf.append("L");
                buf.append(at.charAt(at.length()-1));
            } else {
                buf.append(at);
            }
            buf.append("->");
            String goal = (String) cargo.get(ATT_GOAL_LOCATION);
            if (goal.contains("Location")) {
                buf.append("L");
                buf.append(goal.charAt(goal.length()-1));
            } else {
                buf.append(goal);
            }
            buf.append("} ");
        }
        buf.append(";\n");
        return buf.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LCRootState that = (LCRootState) o;

        return (cargos != null ? cargos.equals(that.cargos) : that.cargos == null)
                &&
                (copter != null ? copter.equals(that.copter) : that.copter == null);
    }

    @Override
    public int hashCode() {
        int result = copter != null ? copter.hashCode() : 0;
        result = 31 * result + (cargos != null ? cargos.hashCode() : 0);
        return result;
    }


    @Override
    public MutableOOState deepCopy() {
        LCRootState copy = this.copy();
        copy.touchCargos();
        copy.touchCopter();
        return copy;
    }
}
