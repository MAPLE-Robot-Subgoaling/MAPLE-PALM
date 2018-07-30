package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

public class UniversalActionTypeBean extends UniversalActionType {

    public UniversalActionTypeBean() {
        super("ERRORNOTSET");
        this.allActions = null;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        if (allActions == null) {
            this.allActions = Arrays.asList(this.action);
        }
        return allActions;
    }
}
