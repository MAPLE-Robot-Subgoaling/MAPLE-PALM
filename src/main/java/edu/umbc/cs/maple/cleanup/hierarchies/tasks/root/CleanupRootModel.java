package edu.umbc.cs.maple.cleanup.hierarchies.tasks.root;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickAgent;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickBlock;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickState;

import java.util.List;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_REGION;

public class CleanupRootModel implements FullStateModel {

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        return FullStateModel.Helper.deterministicTransition(this,s,a);
    }

    @Override
    public State sample(State s, Action a) {
        PickState ns = (PickState) s.copy();
        if(a.actionName().equals(CleanupRoot.ACTION_PICK_ROOM_AGENT)){
            return agentToRoom(ns, (ObjectParameterizedAction) a);
        }
        return blockToRoom(ns, (ObjectParameterizedAction) a);
    }

    private State agentToRoom(PickState ns, ObjectParameterizedAction a) {
        PickAgent agent = (PickAgent) ns.touchAgent();
        agent.set(ATT_REGION, a.getObjectParameters()[0]);
        return ns;
    }

    public State blockToRoom(PickState s, ObjectParameterizedAction a){
        PickAgent agent = (PickAgent) s.touchAgent();
        PickBlock block = (PickBlock) s.touchBlock(a.getObjectParameters()[0]);
        agent.set(ATT_REGION, a.getObjectParameters()[1]);
        block.set(ATT_REGION, a.getObjectParameters()[1]);
        return s;
    }
}