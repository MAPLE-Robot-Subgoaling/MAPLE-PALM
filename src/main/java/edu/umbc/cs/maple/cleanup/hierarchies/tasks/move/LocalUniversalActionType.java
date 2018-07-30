package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import jdk.vm.ci.meta.Local;

public class LocalUniversalActionType extends UniversalActionType {
    public LocalUniversalActionType(String typeName) {
        super(typeName);
    }

    public LocalUniversalActionType(Action action) {
        super(action);
    }

    public LocalUniversalActionType(String typeName, Action action) {
        super(typeName, action);
    }

    public LocalUniversalActionType(){
        super(null,null);
    }
}
