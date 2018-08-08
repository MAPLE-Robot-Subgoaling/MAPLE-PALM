package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.liftCopter.ThrustType.ThrustAction;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.LCNavAgent;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.LCNavState;
import edu.umbc.cs.maple.liftCopter.state.*;
import edu.umbc.cs.maple.liftCopter.state.LiftCopterCargo;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;
public class LCNavModel implements FullStateModel{

    /**
     * the array saying how the probabilities are distributed
     */

    /**
     * creates a stochastic non fickle liftCopter model
     */
    public LCNavModel() {  }

    @Override
    public State sample(State s, Action a) {
        List<StateTransitionProb> stpList = this.stateTransitions(s,a);
        double roll = RandomFactory.getMapped(0).nextDouble();
//         System.out.println(roll);
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
        int action = actionInd(a);
        LCNavState liftCopterS = (LCNavState) s;

        if(action <= IND_IDLE){
            movement(liftCopterS, action, tps, a);
        }
        return tps;
    }

    /**
     * add all resulting states given a movement action
     * @param s the current state
     * @param action the index of the selected movement action
     * @param tps a list of state transition probabilities to add to
     */
    public void movement(LCNavState s, int action, List<StateTransitionProb> tps, Action a){
        LCNavState ns = s.copy();
        double force = 0.0D;
        double direction = 0.0D;

        double tx = (double) s.touchCopter().get(ATT_X);
        double ty = (double) s.touchCopter().get(ATT_Y);
        double tvx = (double) s.touchCopter().get(ATT_VX);
        double tvy = (double) s.touchCopter().get(ATT_VY);


//        for(int outcome = 0; outcome < moveProbabilities.length; outcome++){
//            double p = moveProbabilities[outcome];
        if (a instanceof ThrustAction) {
            force = ((ThrustAction)a).thrust;
            direction = ((ThrustAction)a).direction;
        }


        this.updateMotion(ns, force, direction);

        tps.add(new StateTransitionProb(ns, 1));
    }



    /**
     * resolve continuous motion
     * @param s the current state
     * @param thrust
     * @param direction
     */
    protected void updateMotion(LCNavState s, double thrust, double direction) {

        double ti = 1.0D;
        double tt = ti * ti;
        LCNavAgent agent = s.touchCopter();
        double x = (double)agent.get(ATT_X);
        double y = (double)agent.get(ATT_Y);
        double h = (double)agent.get(ATT_H);
        double w = (double)agent.get(ATT_W);
        double vx = (double)agent.get(ATT_VX);
        double vy = (double)agent.get(ATT_VY);
        double tx = Math.cos(direction) * thrust;
        double ty = Math.sin(direction) * thrust;
//        System.out.println(tx+","+ty);
        double nx = x + vx * ti + 0.5D * tx * tt;
        double ny = y + vy * ti + 0.5D * ty * tt;
        double nvx = vx + tx * ti;
        double nvy = vy + ty * ti;
//        System.out.println("Updating Motion: \n " +
//                "\t old State: Coords: ("+x +","+y +") velocity: ("+vx +","+vy +") \n" +
//                "\t new State: Coords: ("+nx+","+ny+") velocity: ("+nvx+","+nvy+")");

        if (nvx > PHYS_MAX_VX) {
            nvx = PHYS_MAX_VX;
        } else if (nvx < -PHYS_MAX_VX) {
            nvx = -PHYS_MAX_VX;
        }

        if (nvy > PHYS_MAX_VY) {
            nvy = PHYS_MAX_VY;
        } else if (nvy < -PHYS_MAX_VY) {
            nvy = -PHYS_MAX_VY;
        }

        agent.set(ATT_X, nx);
        agent.set(ATT_Y, ny);
        agent.set(ATT_VX, nvx);
        agent.set(ATT_VY, nvy);

        List<ObjectInstance> walls = s.objectsOfClass(CLASS_WALL);
        double aw = (double)agent.get(ATT_W);
        double ah = (double)agent.get(ATT_H);
        for (ObjectInstance wall : walls) {
            double ww = (double) wall.get(ATT_WIDTH);
            double wh = (double) wall.get(ATT_HEIGHT);
            double wx = (double) wall.get(ATT_START_X);
            double wy = (double) wall.get(ATT_START_Y);
//            System.out.println("Compare: \n" +
////                    "\t a:"+ax+","+ay+","+ah+","+aw+"\n" +
////                    "\t w:"+wall.name() + ","+wx+","+wy+","+wh+","+ww
////            );
            if (wx < nx + aw &&
                    wx + ww > nx &&
                    wy < ny + ah &&
                    wy + wh > ny) {
                System.out.println("Crashed into "+wall.name());
                agent.set(ATT_X, x);
                agent.set(ATT_Y, y);
                agent.set(ATT_VX, 0);
                agent.set(ATT_VY, 0);
                //agent.set(ATT_LOCATION, ATT_VAL_CRASHED);
            }

        }

    }

    /**
     * map a action to its number
     * @param a the action
     * @return the number that represents the action
     */
    public int actionInd(Action a){
        String aname = a.actionName();
        if(aname.startsWith(ACTION_THRUST))
            return IND_THRUST;
        else if(aname.startsWith(ACTION_IDLE))
            return IND_IDLE;
        throw new RuntimeException("Invalid action " + aname);
    }
}
