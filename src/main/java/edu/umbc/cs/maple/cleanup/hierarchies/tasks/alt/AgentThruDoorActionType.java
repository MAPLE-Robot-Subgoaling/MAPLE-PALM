package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.AgentThruDoorGoalPF;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class AgentThruDoorActionType extends ObjectParameterizedActionType {

    public AgentThruDoorActionType() {
        super("ERRORNOTSET", new String[]{});
    }

    public void setParameterClasses(String[] parameterClasses) {
        this.parameterClasses = parameterClasses;
    }

    public void setParameterOrderGroup(String[] parameterOrderGroup) {
        this.parameterOrderGroup = parameterOrderGroup;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        CleanupState state = (CleanupState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        boolean notInDoor = AgentThruDoorGoalPF.isTrue(state, params);
        return !notInDoor;
    }

}