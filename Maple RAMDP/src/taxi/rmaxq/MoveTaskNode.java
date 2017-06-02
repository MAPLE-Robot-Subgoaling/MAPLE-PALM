package taxi.rmaxq;

import burlap.mdp.core.action.ActionType;
import rmaxq.framework.PrimitiveTaskNode;


public class MoveTaskNode extends PrimitiveTaskNode{

    public MoveTaskNode(ActionType a){
        this.setActionType(a);
    }
    
}
