package taxi.hierarchies.tasks.put;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.put.state.TaxiPutAgent;
import taxi.hierarchies.tasks.put.state.TaxiPutPassenger;
import taxi.hierarchies.tasks.put.state.TaxiPutState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class TaxiPutModel implements FullStateModel {

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
        TaxiPutState state = (TaxiPutState) s;

        if(a.actionName().startsWith(ACTION_PUTDOWN)) {
            putdown(state, (ObjectParameterizedAction)a, tps);
        } else if(a.actionName().startsWith(ACTION_NAV)) {
            navigate(state, (ObjectParameterizedAction)a, tps);
        }
        return tps;
    }

    /**
     * put the requested passenger into the taxi
     * @param s the current state
     * @param a the get action type
     * @param tps the list of transition probabilities
     */
    public void putdown(TaxiPutState s, ObjectParameterizedAction a, List<StateTransitionProb> tps) {
        TaxiPutState ns = s.copy();
        String passenger = a.getObjectParameters()[0];

        TaxiPutPassenger np = ns.touchPassenger(passenger);
        ObjectInstance taxi = s.objectsOfClass(CLASS_TAXI).get(0);
        String taxiLocation = (String) taxi.get(ATT_LOCATION);
        np.set(ATT_LOCATION, taxiLocation);

        tps.add(new StateTransitionProb(ns, 1));
    }

    /**
     * put the requested passenger into the taxi
     * @param s the current state
     * @param a the get action type
     * @param tps the list of transition probabilities
     */
    public void navigate(TaxiPutState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
        TaxiPutState ns = s.copy();
        String goal = a.getObjectParameters()[0];

        TaxiPutAgent nt = ns.touchTaxi();
        nt.set(ATT_LOCATION, goal);

        tps.add(new StateTransitionProb(ns, 1));
    }
}
