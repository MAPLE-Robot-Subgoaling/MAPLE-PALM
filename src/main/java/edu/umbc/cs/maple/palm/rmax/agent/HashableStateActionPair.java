package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.statehashing.HashableState;

import java.util.Objects;

public class HashableStateActionPair {

    protected HashableState hs;
    protected String actionName;

    public HashableStateActionPair(HashableState hs, String actionName) {
        this.hs = hs;
        this.actionName = actionName;
    }

    public HashableState getHs() {
        return hs;
    }

    public void setHs(HashableState hs) {
        this.hs = hs;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }


    @Override
    public String toString() {
        String a = actionName == null ? "no action" : actionName;
        String s = hs == null ? "no hs" : hs.s().toString();
        return "{" + a +
                ", " + s +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashableStateActionPair that = (HashableStateActionPair) o;
        return Objects.equals(hs, that.hs) &&
                Objects.equals(actionName, that.actionName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hs, actionName);
    }
}