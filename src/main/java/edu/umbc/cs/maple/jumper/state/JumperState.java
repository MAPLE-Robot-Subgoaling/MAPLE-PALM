package edu.umbc.cs.maple.jumper.state;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.*;

public class JumperState implements OOState {

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
    public ObjectInstance object(String s) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<ObjectInstance> objects()  {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>();
        if (agent != null) obs.add(agent);
        obs.addAll(targets.values());
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String s) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object o) {
        throw new RuntimeException("not implemented");
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
}