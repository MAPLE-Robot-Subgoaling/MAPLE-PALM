package edu.umbc.cs.maple.liftCopter;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.liftCopter.ThrustType.ThrustAction;
import edu.umbc.cs.maple.liftCopter.state.*;
import edu.umbc.cs.maple.liftCopter.state.LiftCopterCargo;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;
public class LiftCopterModel implements FullStateModel{

    /**
     * the array saying how the probabilities are distributed
     */
    private double[][] moveProbability;

    /**
     * creates a stochastic non fickle liftCopter model
     * @param moveprob the array of movement probabilities
     */
    public LiftCopterModel(double[][] moveprob){
        this.moveProbability = moveprob;
    }

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
        LiftCopterState liftCopterS = (LiftCopterState) s;

        if(action <= IND_IDLE){
            movement(liftCopterS, action, tps, a);
        }else if(action == IND_PUTDOWN){
            putdown(liftCopterS, (ObjectParameterizedAction)a, tps);
        }else if(action == IND_PICKUP){
            pickup(liftCopterS, (ObjectParameterizedAction)a, tps);
        }


        return tps;
    }

    /**
     * add all resulting states given a movement action
     * @param s the current state
     * @param action the index of the selected movement action
     * @param tps a list of state transition probabilities to add to
     */
    public void movement(LiftCopterState s, int action, List<StateTransitionProb> tps, Action a){
        LiftCopterState ns = s.copy();
        double[] moveProbabilities = this.moveProbability[action];
        double force = 0.0D;
        double direction = 0.0D;

        double tx = (double) s.getCopter().get(ATT_X);
        double ty = (double) s.getCopter().get(ATT_Y);
        double tvx = (double) s.getCopter().get(ATT_VX);
        double tvy = (double) s.getCopter().get(ATT_VY);


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
     * put cargo inside liftCopter
     * @param s the current state
     * @param a the Cargo parameterized pickup action
     * @param tps a list of state transition probabilities to add to
     */
    public void pickup(LiftCopterState s, ObjectParameterizedAction a, List<StateTransitionProb> tps) {
        String p = a.getObjectParameters()[0];
        LiftCopterState ns = s.copy();

        double tx = (double) s.getCopter().get(ATT_X);
        double ty = (double) s.getCopter().get(ATT_Y);

        double lx = (double) s.object(p).get(ATT_X);
        double ly = (double) s.object(p).get(ATT_Y);
        double lh = (double) s.object(p).get(ATT_H);
        double lw = (double) s.object(p).get(ATT_W);

        boolean inliftCopter = (boolean) s.object(p).get(ATT_PICKED_UP);

        if (lx + lw/2 >= tx && lx - lw/2 <= tx && ly + lh/2 >= ty && ly - lh/2 <= ty && !inliftCopter) {
            LiftCopterCargo np = ns.touchCargo(p);
            np.set(ATT_PICKED_UP, true);
        }
        this.updateMotion(ns, 0.0D, 0.0D);
        tps.add(new StateTransitionProb(ns, 1.));
    }

    /**
     * put cargo down if the liftCopter is occupied and at a depot
     * @param s the current state
     * @param a the Cargo parameterized putdown action
     * @param tps a list of state transition probabilities to add to
     */
    public void putdown(LiftCopterState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
        String p = a.getObjectParameters()[0];
        LiftCopterState ns = s.copy();
        double tx = (double) s.getCopter().get(ATT_X);
        double ty = (double) s.getCopter().get(ATT_Y);

        if((boolean) s.object(p).get(ATT_PICKED_UP)) {
            for (ObjectInstance location : s.objectsOfClass(CLASS_LOCATION)) {
                double lx = (double) location.get(ATT_X);
                double ly = (double) location.get(ATT_Y);
                double lh = (double) location.get(ATT_H);
                double lw = (double) location.get(ATT_W);
                if (lx + lw/2 >= tx &&
                        lx - lw/2 <= tx &&
                        ly + lh/2 >= ty &&
                        ly - lh/2 <= ty) {
                    LiftCopterCargo np = ns.touchCargo(p);
                    np.set(ATT_PICKED_UP, false);
                    np.set(ATT_X, lx);
                    np.set(ATT_Y, ly);
                    break;
                }
            }
        }
        this.updateMotion(ns, 0.0D, 0.0D);
        tps.add(new StateTransitionProb(ns, 1.));
    }

    /**
     * resolve continuous motion
     * @param s the current state
     * @param thrust
     * @param direction
     */
    protected void updateMotion(LiftCopterState s, double thrust, double direction) {
        if(hasLiftCopterCrashed(s)) return;
        double ti = 1.0D;
        double tt = ti * ti;
        LiftCopterAgent agent = s.touchCopter();
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
        hasLiftCopterCrashed(s);
        for(ObjectInstance passenger : s.objectsOfClass(CLASS_CARGO)){
            boolean inTaxi = (boolean) passenger.get(ATT_PICKED_UP);
            if(inTaxi){
                String passengerName = passenger.name();
                LiftCopterCargo np = s.touchCargo(passengerName);
                np.set(ATT_X, nx-((double)agent.get(ATT_W)/2));
                np.set(ATT_Y, ny-((double)agent.get(ATT_H)/2));
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
        else if(aname.startsWith(ACTION_PICKUP))
            return IND_PICKUP;
        else if(aname.startsWith(ACTION_PUTDOWN))
            return IND_PUTDOWN;
        throw new RuntimeException("Invalid action " + aname);
    }
    public boolean hasLiftCopterCrashed(LiftCopterState s){
        List<ObjectInstance> walls = s.objectsOfClass(CLASS_WALL);
        double ax = (double) s.touchCopter().get(ATT_X);
        double ay = (double) s.touchCopter().get(ATT_Y);
        double ah = (double) s.touchCopter().get(ATT_H);
        double aw = (double) s.touchCopter().get(ATT_W);
        for (ObjectInstance wall : walls) {
            double ww = (double) wall.get(ATT_WIDTH);
            double wh = (double) wall.get(ATT_HEIGHT);
            double wx = (double) wall.get(ATT_START_X);
            double wy = (double) wall.get(ATT_START_Y);
//            System.out.println("Compare: \n" +
////                    "\t a:"+ax+","+ay+","+ah+","+aw+"\n" +
////                    "\t w:"+wall.name() + ","+wx+","+wy+","+wh+","+ww
////            );
            if (wx < ax + aw &&
                    wx + ww > ax &&
                    wy < ay + ah &&
                    wy + wh > ay) {
                System.out.println("Crashed into "+wall.name());
                s.getCopter().set(ATT_LOCATION, ATT_VAL_CRASHED);
                return true;
            }

        }
        return false;
    }
}
