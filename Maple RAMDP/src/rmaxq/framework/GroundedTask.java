package rmaxq.framework;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Created by ngopalan on 5/14/16.
 */

public class GroundedTask {
    TaskNode t;

    Action action;

    public Action getAction() {
        return action;
    }

    public GroundedTask(TaskNode t, Action a){
        this.t = t;
        this.action = a;
    }


    public String actionName(){
        return action.actionName();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof GroundedTask)) {
            return false;
        }


        GroundedTask o = (GroundedTask) other;


//        if (!this.t.name().equals(o.t.name())) {
//            return false;
//        }
//
        if(!this.action.actionName().equals(o.action.actionName())){
            return false; 
        }

//        if(this.action instanceof ObjectParameterizedAction){
//            if(!(o.action instanceof ObjectParameterizedAction)){
//                return false;
//            }
//
//            String[] params_this = ((ObjectParameterizedAction)this.action).getObjectParameters();
//            String[] params_other = ((ObjectParameterizedAction)o.action).getObjectParameters();
//
//            if(params_other.length!=params_this.length){
//                return false;
//            }
//
//            boolean flag = true;
//
//            for(int i=0;i<params_other.length;i++){
//                if(!params_other[i].equals(params_this[i])){
//                    flag = false;
//                    break;
//                }
//            }
//
//            return flag;
//       
//        }
        return true;

    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(31, 7);
        hashCodeBuilder.append(action.actionName());//.append(t)
        return hashCodeBuilder.toHashCode();
    }
}
