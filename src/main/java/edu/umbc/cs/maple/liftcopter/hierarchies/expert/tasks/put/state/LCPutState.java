package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.put.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.LCGetPutState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class LCPutState extends LCGetPutState implements DeepCopyForShallowCopyState {

    //this state has cargos and depots
    private LCPutAgent agent;
    private Map<String, LCPutCargo> cargos;
    private Map<String, LCPutLocation> locations;

    public LCPutState(LCPutAgent agent, List<LCPutCargo> pass, List<LCPutLocation> locs) {
        this.agent = agent;

        this.cargos = new HashMap<>();
        for(LCPutCargo p : pass){
            this.cargos.put(p.name(), p);
        }

        this.locations = new HashMap<>();
        for(LCPutLocation loc : locs){
            this.locations.put(loc.name(), loc);
        }
    }

    private LCPutState(LCPutAgent agent, Map<String, LCPutCargo> pass, Map<String, LCPutLocation> locs) {
        this.agent = agent;
        this.cargos = pass;
        this.locations = locs;
    }

    @Override
    public int numObjects() {
        int total = agent == null ? 0 : 1;
        total += cargos.size();
        total += locations.size();
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(agent != null && oname.equals(agent.name())) {
            return agent;
        }

        ObjectInstance o = cargos.get(oname);
        if(o != null) {
            return o;
        }

        o = locations.get(oname);
        if(o != null) {
            return o;
        }

        return null;
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obj = new ArrayList<ObjectInstance>();
        if (agent != null) { obj.add(agent); }
        obj.addAll(cargos.values());
        obj.addAll(locations.values());
        return obj;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_AGENT)) {
            return agent == null ? new ArrayList<>() : Arrays.<ObjectInstance>asList(agent);
        } if(oclass.equals(CLASS_CARGO)) {
            return new ArrayList<>(cargos.values());
        } if(oclass.equals(CLASS_LOCATION)) {
            return new ArrayList<>(locations.values());
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
    public LCPutState copy() {
        return new LCPutState(touchAgent(), touchCargos(), touchLocations());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof LCPutAgent || o.className().equals(CLASS_AGENT)) {
            agent = (LCPutAgent)o;
        } else if(o instanceof LCPutCargo || o.className().equals(CLASS_CARGO)){
            touchCargos().put(o.name(), (LCPutCargo) o);
        } else if(o instanceof LCPutLocation || o.className().equals(CLASS_LOCATION)){
            touchLocations().put(o.name(), (LCPutLocation) o);
        } else {
            throw new RuntimeException("Can only add certain objects to state.");
        }
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof LCPutAgent) {
            touchAgent();
            agent = null;
        } else if (objectInstance instanceof LCPutCargo) {
            touchCargo(oname);
            cargos.remove(oname);
        } else if (objectInstance instanceof LCPutLocation) {
            touchLocation(oname);
            locations.remove(oname);
        } else {
            throw new RuntimeException("Error: unknown object of name: " + oname);
        }
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    //touch methods allow a shallow copy of states and a copy of objects only when modified
    public LCPutAgent touchAgent() {
        if (agent != null) { this.agent = agent.copy(); }
        return agent;
    }

    public LCPutCargo touchCargo(String passName){
        LCPutCargo p = cargos.get(passName).copy();
        touchCargos().remove(passName);
        cargos.put(passName, p);
        return p;
    }

    public Map<String, LCPutCargo> touchCargos(){
        this.cargos = new HashMap<>(cargos);
        return cargos;
    }


    public LCPutLocation touchLocation(String locName){
        LCPutLocation loc = locations.get(locName).copy();
        touchLocations().remove(locName);
        locations.put(locName, loc);
        return loc;
    }

    public Map<String, LCPutLocation> touchLocations(){
        this.locations = new HashMap<>(locations);
        return locations;
    }

    public Object getAgentAtt(String attName) {
        if (agent == null) {
            return null;
        }
        return agent.get(attName);
    }

    @Override
    public String toString(){
        String out = "{ " + this.getClass().getSimpleName() + "\n";

        if (agent != null) {
            out += agent.toString() + "\n";
        }

        for(LCPutCargo p : cargos.values()){
            out += p.toString() + "\n";
        }
        out += "}";
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LCPutState that = (LCPutState) o;
        return Objects.equals(agent, that.agent) &&
                Objects.equals(cargos, that.cargos) &&
                Objects.equals(locations, that.locations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(agent, cargos, locations);
    }

    @Override
    public MutableOOState deepCopy() {
        LCPutState copy = this.copy();
        copy.touchAgent();
        copy.touchCargos();
        copy.touchLocations();
        return copy;
    }
}
