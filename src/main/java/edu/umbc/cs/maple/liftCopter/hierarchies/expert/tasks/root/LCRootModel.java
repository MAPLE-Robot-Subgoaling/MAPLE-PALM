package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.LCRootCargo;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.LCRootState;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCRootModel implements FullStateModel {

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
        List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>();
        LCRootState state = (LCRootState) s;

        if(a.actionName().startsWith(ACTION_GET)) {
            get(state, (ObjectParameterizedAction) a, tps);
        } else if(a.actionName().startsWith(ACTION_PUT)) {
            put(state, (ObjectParameterizedAction) a, tps);
        }
        return tps;
    }

    /**
     * get the requested cargo into the taxi
     * @param s the current state
     * @param tps the list of transition probabilities
     */
    public void get(LCRootState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
        LCRootState ns = s.copy();
        String cargoName = a.getObjectParameters()[0];
        LCRootCargo np = ns.touchCargo(cargoName);
        np.set(ATT_LOCATION, ATT_VAL_PICKED_UP);
        tps.add(new StateTransitionProb(ns, 1));
    }

    /**
     * put the requested cargo into the taxi
     * @param s the current state
     * @param tps the list of transition probabilities
     */
    public void put(LCRootState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
        LCRootState ns = s.copy();
        String cargoName = a.getObjectParameters()[0];
//		MutableObject cargo = (MutableObject) s.object(cargoName);
        LCRootCargo np = ns.touchCargo(cargoName);
        String nameOfCargoGoalLocation = (String) np.get(ATT_GOAL_LOCATION);
        // put the cargo in their own goal location
        np.set(ATT_LOCATION, nameOfCargoGoalLocation);
        tps.add(new StateTransitionProb(ns, 1));
    }
}
