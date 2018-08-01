package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state.LCGetAgent;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state.LCGetCargo;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state.LCGetState;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCGetModel implements FullStateModel {

    @Override
    public State sample(State s, Action a) {
        List<StateTransitionProb> stpList = this.stateTransitions(s,a);
        double roll = RandomFactory.getMapped(0).nextDouble();
        double curSum = 0.;
        for(int i = 0; i < stpList.size(); i++){
            curSum += stpList.get(i).p;
            if(roll < curSum){
                return stpList.get(i).s;
            }
        }
        throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        List<StateTransitionProb> tps = new ArrayList<>();
        LCGetState state = (LCGetState) s;

        if(a.actionName().startsWith(ACTION_PICKUP)) {
            pickup(state, (ObjectParameterizedAction) a, tps);
        } else if(a.actionName().startsWith(ACTION_NAV)) {
            navigate(state, (ObjectParameterizedAction)a, tps);
        }
        return tps;
    }

    /**
     * put the requested cargo into the agent
     * @param s the current state
     * @param a the get action type
     * @param tps the list of transition probabilities
     */
    public void pickup(LCGetState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
        LCGetState ns = s.copy();
        String cargo = a.getObjectParameters()[0];

        LCGetCargo np = ns.touchCargo(cargo);
        np.set(ATT_LOCATION, ATT_VAL_PICKED_UP);

        tps.add(new StateTransitionProb(ns, 1));
    }

    /**
     * put the requested cargo into the agent
     * @param s the current state
     * @param a the get action type
     * @param tps the list of transition probabilities
     */
    public void navigate(LCGetState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
        LCGetState ns = s.copy();
        String goal = a.getObjectParameters()[0];

        LCGetAgent nt = ns.touchAgent();

        nt.set(ATT_LOCATION, goal);

        tps.add(new StateTransitionProb(ns, 1.));
    }
}
