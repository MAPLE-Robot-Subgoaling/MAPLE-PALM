package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.LCGetPutState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCGetState extends LCGetPutState implements DeepCopyForShallowCopyState {

    //this state has passengers and depots
    private LCGetAgent agent;
    private Map<String, LCGetCargo> cargos;
    private Map<String, LCGetLocation> locations;

    public LCGetState(LCGetAgent agent, List<LCGetCargo> pass, List<LCGetLocation> locs) {
        this.agent = agent;

        this.cargos = new HashMap<>();
        for(LCGetCargo p : pass){
            this.cargos.put(p.name(), p);
        }

        this.locations = new HashMap<>();
        for(LCGetLocation loc : locs){
            this.locations.put(loc.name(), loc);
        }
    }

    private LCGetState(LCGetAgent agent, Map<String, LCGetCargo> pass, Map<String, LCGetLocation> locs) {
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
        if(o != null)
            return o;

        o = locations.get(oname);
        if(o != null)
            return o;

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
        if(oclass.equals(CLASS_AGENT))
            return agent == null ? new ArrayList<ObjectInstance>() : Arrays.<ObjectInstance>asList(agent);
        if(oclass.equals(CLASS_CARGO))
            return new ArrayList<ObjectInstance>(cargos.values());
        if(oclass.equals(CLASS_LOCATION))
            return new ArrayList<ObjectInstance>(locations.values());
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
    public LCGetState copy() {
        return new LCGetState(touchAgent(), touchCargos(), touchLocations());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(key.obName.equals(agent.name())) {
            touchAgent().set(variableKey, value);
        } else if(cargos.get(key.obName) != null){
            touchCargo(key.obName).set(variableKey, value);
        } else if(locations.get(key.obName) != null){
            touchLocation(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof LCGetAgent || o.className().equals(CLASS_AGENT)) {
            agent = (LCGetAgent)o;
        } else if(o instanceof LCGetCargo || o.className().equals(CLASS_CARGO)){
            touchCargos().put(o.name(), (LCGetCargo) o);
        } else if(o instanceof LCGetLocation || o.className().equals(CLASS_LOCATION)){
            touchLocations().put(o.name(), (LCGetLocation) o);
        } else {
            throw new RuntimeException("Can only add certain objects to state.");
        }
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof LCGetAgent) {
            touchAgent();
            agent = null;
        } else if (objectInstance instanceof LCGetCargo) {
            touchCargo(oname);
            cargos.remove(oname);
        } else if (objectInstance instanceof LCGetLocation) {
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
    public LCGetAgent touchAgent() {
        if (this.agent != null) { this.agent = agent.copy(); }
        return agent;
    }

    public LCGetCargo touchCargo(String name){
        LCGetCargo p = cargos.get(name).copy();
        touchCargos().remove(name);
        cargos.put(name, p);
        return p;
    }

    public Map<String, LCGetCargo> touchCargos(){
        this.cargos = new HashMap<String, LCGetCargo>(cargos);
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

    public LCGetLocation touchLocation(String name){
        LCGetLocation loc = locations.get(name).copy();
        touchLocations().remove(name);
        locations.put(name, loc);
        return loc;
    }

    public Map<String, LCGetLocation> touchLocations(){
        this.locations = new HashMap<String, LCGetLocation>(locations);
        return locations;
    }

    //get values from objects
    public String[] getLocations(){
        String[] ret = new String[locations.size()];
        int i = 0;
        for(String name : locations.keySet())
            ret[i++] = name;
        return ret;
    }

    public Object getAgentAtt(String attName) {
        if (agent == null) { return null; }
        return agent.get(attName);
    }

    public Object getCargoAtt(String passName, String attName){
        return cargos.get(passName).get(attName);
    }

    public Object getLocationAtt(String locName, String attName) {
        return locations.get(locName).get(attName);
    }

    @Override
    public String toString(){
        String out = "{ " + this.getClass().getSimpleName() + "\n";

        if (agent != null) {
            out += agent.toString() + "\n";
        }

        for(LCGetCargo p : cargos.values()){
            out += p.toString() + "\n";
        }

        for(LCGetLocation loc : locations.values()){
            out += loc.toString() + "\n";
        }
        out += "}";
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LCGetState that = (LCGetState) o;

        if (agent != null ? !agent.equals(that.agent) : that.agent != null) return false;
        if (cargos != null ? !cargos.equals(that.cargos) : that.cargos != null) return false;
        return locations != null ? locations.equals(that.locations) : that.locations == null;
    }

    @Override
    public int hashCode() {
        int result = agent != null ? agent.hashCode() : 0;
        result = 31 * result + (cargos != null ? cargos.hashCode() : 0);
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        return result;
    }

    @Override
    public MutableOOState deepCopy() {
        LCGetState copy = this.copy();
        copy.touchAgent();
        copy.touchCargos();
        copy.touchLocations();
        return copy;
    }
}
