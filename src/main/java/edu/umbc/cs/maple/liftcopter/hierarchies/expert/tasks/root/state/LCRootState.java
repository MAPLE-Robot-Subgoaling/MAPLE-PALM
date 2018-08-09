package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class LCRootState implements MutableOOState, DeepCopyForShallowCopyState {

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

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obj = new ArrayList<>();
        obj.addAll(cargos.values());
        if(copter != null) obj.add(copter);
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
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object get(Object variableKey) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public LCRootState copy() {
        return new LCRootState(touchCargos(), touchCopter());
    }


    @Override
    public MutableState set(Object variableKey, Object value) {
        throw new RuntimeException("not implemented");
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
        return Objects.equals(cargos, that.cargos) &&
                Objects.equals(copter, that.copter);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cargos, copter);
    }

    @Override
    public MutableOOState deepCopy() {
        LCRootState copy = this.copy();
        copy.touchCargos();
        copy.touchCopter();
        return copy;
    }
}
