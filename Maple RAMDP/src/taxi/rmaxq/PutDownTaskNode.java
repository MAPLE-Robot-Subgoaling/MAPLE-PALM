package taxi.rmaxq;

import burlap.mdp.core.action.ActionType;
import rmaxq.framework.PrimitiveTaskNode;

/**
 * Created by ngopalan on 8/14/16.
 */
public class PutDownTaskNode extends PrimitiveTaskNode{

    public PutDownTaskNode(ActionType a){
        this.setActionType(a);
    }

}
