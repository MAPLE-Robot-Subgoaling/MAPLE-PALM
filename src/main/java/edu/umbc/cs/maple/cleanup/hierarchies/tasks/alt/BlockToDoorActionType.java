package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs.BlockToDoorGoalPF;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class BlockToDoorActionType extends ObjectParameterizedActionType {

    public BlockToDoorActionType(){
        super("ERRORNOTSET", new String[]{});
    }

    public void setParameterClasses(String[] parameterClasses){
        this.parameterClasses = parameterClasses;
    }

    public void setParameterOrderGroup(String[] parameterOrderGroup) {
        this.parameterOrderGroup = parameterOrderGroup;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        CleanupState state = (CleanupState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        boolean inDoor = BlockToDoorGoalPF.isTrue(state, params);
        return !inDoor;
    }

}
