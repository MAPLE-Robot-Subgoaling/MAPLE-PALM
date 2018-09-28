package edu.umbc.cs.maple.jumper.state;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.utilities.MutableObjectInstance;

import java.util.*;

import static edu.umbc.cs.maple.jumper.JumperConstants.CLASS_AGENT;
import static edu.umbc.cs.maple.jumper.JumperConstants.CLASS_TARGET;

public class JumperState implements OOState, MutableState {

    protected JumperAgent agent;
    protected Map<String, JumperTarget> targets;

    public JumperState() {
        // for de/serialization
    }

    public JumperState(JumperAgent agent, JumperTarget target) {
        this.agent = agent;
        this.targets = new LinkedHashMap<>();
        this.targets.put(target.name(), target);
    }

    public JumperState(JumperState source) {
        this.agent = source.agent;
        this.targets = source.targets;
    }

    @Override
    public int numObjects() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ObjectInstance object(String oName) {

        if (agent != null && oName.equals(agent.name())) {
            return agent;
        }

        ObjectInstance target = targets.get(oName);
        if (target != null) { return target; }

        throw new RuntimeException("Error: unknown object name " + oName);
    }

    @Override
    public List<ObjectInstance> objects()  {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>();
        if (agent != null) obs.add(agent);
        obs.addAll(targets.values());
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_AGENT)) {
            return Arrays.asList(agent);
        } else if(oclass.equals(CLASS_TARGET)) {
            return new ArrayList<>(targets.values());
        } else {
            throw new RuntimeException("No object class " + oclass);
        }
    }

    @Override
    public List<Object> variableKeys() {
        List<Object> vars = new ArrayList<>();
        for (ObjectInstance objectInstance : objects()) {
            String name = objectInstance.name();
            for (Object key : objectInstance.variableKeys()) {
                String var = name + ":" + key;
                vars.add(var);
            }
        }
        return vars;
    }

    @Override
    public Object get(Object o) {
        return OOStateUtilities.get(this, o);
    }

    @Override
    public State copy() {
        return new JumperState(this);
    }

    public JumperAgent getAgent() {
        return agent;
    }

    public Map<String,JumperTarget> getTargets() {
        return targets;
    }

    public JumperAgent touchAgent() {
        if (agent == null) return null;
        this.agent = (JumperAgent) agent.copy();
        return agent;
    }

    public Map<String, JumperTarget> touchTargets() {
        this.targets = new HashMap<>(targets);
        return targets;
    }

    public JumperTarget touchTarget(String name) {
        JumperTarget n = (JumperTarget) targets.get(name).copy();
        touchTargets().remove(name);
        targets.put(name, n);
        return n;
    }

    public ObjectInstance touch(String name) {
        ObjectInstance object = object(name);
        if (object instanceof JumperAgent) {
            object = touchAgent();
        } else if (object instanceof JumperTarget) {
            object = touchTarget(name);
        } else {
            throw new RuntimeException("Error: unknown object named " + name);
        }
        return object;
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        String[] split = ((String) variableKey).split(":");
        String name = split[0];
        String attribute = split[1];
        MutableObjectInstance objectInstance = (MutableObjectInstance) touch(name);
        objectInstance.set(attribute, value);
        return this;
    }

    @Override
    public String toString() {
        return "JumperState{" +
                "agent=" + agent +
                ", targets=" + targets +
                '}';
    }
}