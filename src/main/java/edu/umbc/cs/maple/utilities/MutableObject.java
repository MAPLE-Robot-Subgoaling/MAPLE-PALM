package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.state.MutableState;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MutableObject implements MutableObjectInstance, Serializable {

    protected Map<String, Object> values = new HashMap<String, Object>();

    protected String name;

    @Override
    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public Object get(Object variableKey) {
        return this.values.get(variableKey);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        this.values.put(variableKey.toString(), value);
        return this;
    }

    @Override
    public MutableObject copy() {
        return (MutableObject) this.copyWithName(name());
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
//		buf.append(className()).append(":").append(name());
        buf.append(name());
        buf.append(" {");
        List<Object> keys = this.variableKeys();
        for(Object key : keys){
            Object value = this.get(key);
            buf.append(" ").append(key.toString()).append(": {");
            if (value == null) {
                buf.append("unset");
            } else {
                buf.append(value.toString());
            }
            buf.append("}");
        }
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;

        MutableObject o = (MutableObject) other;
        for (Object key : variableKeys()) {
            if (!get(key).equals(o.get(key))) {
                return false;
            }
        }
        return name.equals(o.name);
    }

    protected void mirror(MutableObject other) {
        if (getClass() != other.getClass()) { throw new RuntimeException("ERROR: attempt to mirror objects of different classes"); }
        if (!variableKeys().equals(other.variableKeys())) {
            throw new RuntimeException("ERROR: attempt to mirror objects with different variable keys");
        }
        for (Object key : variableKeys()) {
            this.set(key, other.get(key));
        }
    }
}
