package taxi.hierarchies.tasks.nav;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.hierarchies.tasks.nav.state.TaxiNavAgent;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;
import taxi.hierarchies.tasks.nav.state.TaxiNavWall;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

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

        int tx = (int)state.getTaxiAtt(ATT_X);
        int ty = (int)state.getTaxiAtt(ATT_Y);

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
        int tx = (int)s.getTaxiAtt(ATT_X);
        int ty = (int)s.getTaxiAtt(ATT_Y);
        int nx = tx + dx;
        int ny = ty + dy;
        TaxiNavState ns = s.copy();
        TaxiNavAgent nt = ns.touchTaxi();

        for (TaxiNavWall wall : ns.getWallObjects()) {
            if (wall.blocksMovement(tx, ty, dx, dy)) {
                tps.add(new StateTransitionProb(s, 1.));
                return;
            }
        }

//		for(String wall : ns.getWalls()) {
//			int wx = (int)ns.getWallAtt(wall, ATT_START_X);
//			int wy = (int)ns.getWallAtt(wall, ATT_START_Y);
//			int wl = (int)ns.getWallAtt(wall, ATT_LENGTH);
//			boolean wh = (boolean)ns.getWallAtt(wall, ATT_IS_HORIZONTAL);
//
//			// Wall blocks us. Give up all hope
//			// Haha, eat your heart out, Java Golf
//			if((wh && y == wy && x >= wx && x <= wx + wl) || (!wh && x == wx && y >= wy && y <= y + wl)) {
//				tps.add(new StateTransitionProb(s, 1.));
//				return;
//			}
//		}

        nt.set(ATT_X, nx);
        nt.set(ATT_Y, ny);

        tps.add(new StateTransitionProb(ns, 1.));
    }
}
