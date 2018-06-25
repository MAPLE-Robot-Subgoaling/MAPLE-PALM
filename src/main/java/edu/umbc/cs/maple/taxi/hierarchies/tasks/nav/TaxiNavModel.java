package edu.umbc.cs.maple.taxi.hierarchies.tasks.nav;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.NavStateMapper;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.TaxiNavAgent;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.TaxiNavState;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.TaxiNavWall;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiNavModel implements FullStateModel {

    /**
     * create a taxi nav model
     */
    public TaxiNavModel() { }

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
        TaxiNavState state = new NavStateMapper().mapState(s);

        if(a.actionName().startsWith(ACTION_NORTH)){
            move(state, 0, +1, tps);
        } else if(a.actionName().startsWith(ACTION_SOUTH)) {
            move(state, 0, -1, tps);
        } else if(a.actionName().startsWith(ACTION_EAST)) {
            move(state, +1, 0, tps);
        } else if(a.actionName().startsWith(ACTION_WEST)) {
            move(state, -1, 0, tps);
        }
        return tps;
    }

    /**
     * move the taxi in some direction
     * @param s the current state
     * @param dx change in x position
     * @param dy change in y position
     * @param tps the list of outcomes to add to
     */
    public void move(TaxiNavState s, int dx, int dy, List<StateTransitionProb> tps){
        ObjectInstance taxi = s.objectsOfClass(CLASS_TAXI).get(0);
        int tx = (int) taxi.get(ATT_X);
        int ty = (int) taxi.get(ATT_Y);
        int nx = tx + dx;
        int ny = ty + dy;
        TaxiNavState ns = s.copy();
        TaxiNavAgent nt = ns.touchTaxi();

        for (ObjectInstance objectInstance : ns.objectsOfClass(CLASS_WALL)) {
            TaxiNavWall wall = (TaxiNavWall) objectInstance;
            if (wall.blocksMovement(tx, ty, dx, dy)) {
                tps.add(new StateTransitionProb(s, 1.));
                return;
            }
        }

        nt.set(ATT_X, nx);
        nt.set(ATT_Y, ny);

        tps.add(new StateTransitionProb(ns, 1.));
    }
}
