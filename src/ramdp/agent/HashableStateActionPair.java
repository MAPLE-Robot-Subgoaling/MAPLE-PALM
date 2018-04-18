package ramdp.agent;

import burlap.mdp.core.action.Action;
import burlap.statehashing.HashableState;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashableStateActionPair that = (HashableStateActionPair) o;

        if (hs == null || that.hs == null || hs.hashCode() != that.hs.hashCode()) return false;
        return actionName != null ? actionName.equals(that.actionName) : that.actionName == null;
    }

    @Override
    public int hashCode() {
        int result = hs != null ? hs.hashCode() : 0;
        result = 31 * result + (actionName != null ? actionName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" + actionName +
                ", " + hs.s().toString() +
                '}';
    }
}
