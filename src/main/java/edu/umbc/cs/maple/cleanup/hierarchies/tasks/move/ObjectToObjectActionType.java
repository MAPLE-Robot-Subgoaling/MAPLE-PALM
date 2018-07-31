package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_REGION;

public class ObjectToObjectActionType extends ObjectParameterizedActionType{


    public ObjectToObjectActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public ObjectToObjectActionType(String name, String[] parameterClasses, String[] parameterOrderGroups) {
        super(name, parameterClasses, parameterOrderGroups);
    }

    public ObjectToObjectActionType(){
        super(null, null,null);
    }

    public void setParameterClasses(String[] parameterClasses){
        this.parameterClasses= parameterClasses;
    }

    public void  setParameterOrderGroup(String[] parameterOrderGroup) {
        this.parameterOrderGroup=parameterOrderGroup;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        //get region for object1 and object2 and compare
        OOState state = (OOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String object1Name = params[0];
        String object2Name = params[1];
        ObjectInstance object1 = state.object(object1Name);
        ObjectInstance object2 = state.object(object2Name);
        String currentRegionName = (String) object1.get(ATT_REGION);
        String object2RegionName = (String) object2.get(ATT_REGION);
        boolean shareRegion = currentRegionName.equals(object2RegionName);
        // not applicable if object1 not in same region as object2
        return shareRegion;
    }

}
