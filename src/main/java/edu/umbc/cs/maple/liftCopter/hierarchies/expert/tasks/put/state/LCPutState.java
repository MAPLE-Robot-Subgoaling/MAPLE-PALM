package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.LCGetPutState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

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
        if(o != null)
            return o;

        o = locations.get(oname);
        if(o != null)
            return o;

        return null;
    }

    private List<ObjectInstance> cachedObjectList = null;
    @Override
    public List<ObjectInstance> objects() {
        if (cachedObjectList == null) { cachedObjectList = new ArrayList<ObjectInstance>(); }
        else { return cachedObjectList; }
        List<ObjectInstance> obj = new ArrayList<ObjectInstance>();
        if (agent != null) { obj.add(agent); }
        obj.addAll(cargos.values());
        obj.addAll(locations.values());
        cachedObjectList = obj;
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
    public LCPutState copy() {
        return new LCPutState(touchAgent(), touchCargos(), touchLocations());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(agent != null && key.obName.equals(agent.name())) {
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
        if(o instanceof LCPutAgent || o.className().equals(CLASS_AGENT)) {
            agent = (LCPutAgent)o;
        } else if(o instanceof LCPutCargo || o.className().equals(CLASS_CARGO)){
            touchCargos().put(o.name(), (LCPutCargo) o);
        } else if(o instanceof LCPutLocation || o.className().equals(CLASS_LOCATION)){
            touchLocations().put(o.name(), (LCPutLocation) o);
        } else {
            throw new RuntimeException("Can only add certain objects to state.");
        }
        cachedObjectList = null;
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
        cachedObjectList = null;
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
        this.cargos = new HashMap<String, LCPutCargo>(cargos);
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

    public LCPutLocation touchLocation(String locName){
        LCPutLocation loc = locations.get(locName).copy();
        touchLocations().remove(locName);
        locations.put(locName, loc);
        return loc;
    }

    public Map<String, LCPutLocation> touchLocations(){
        this.locations = new HashMap<String, LCPutLocation>(locations);
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
        if (agent == null) {
            return null;
        }
        return agent.get(attName);
    }

    public Object getCargoAtt(String passname, String attName){
        return cargos.get(passname).get(attName);
    }

    public Object getLocationAtt(String locname, String attName) {
        return locations.get(locname).get(attName);
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
        LCPutState copy = this.copy();
        copy.touchAgent();
        copy.touchCargos();
        copy.touchLocations();
        return copy;
    }
}
