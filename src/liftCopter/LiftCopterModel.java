package liftCopter;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import liftCopter.state.*;
import liftCopter.state.LiftCopterCargo;
import liftCopter.ThrustType.ThrustAction;


import java.util.ArrayList;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;
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
                if (lx + lw/2 >= tx && lx - lw/2 <= tx && ly + lh/2 >= ty && ly - lh/2 <= ty) {
                    LiftCopterCargo np = ns.touchCargo(p);
                    np.set(ATT_PICKED_UP, false);
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

        double ti = 1.0D;
        double tt = ti * ti;
        LiftCopterAgent agent = s.touchCopter();
        double x = (double)agent.get(ATT_X);
        double y = (double)agent.get(ATT_Y);
        double vx = (double)agent.get(ATT_VX);
        double vy = (double)agent.get(ATT_VY);
        double tx = Math.cos(direction) * thrust;
        double ty = Math.sin(direction) * thrust;
        System.out.println(tx+","+ty);
        double nx = x + vx * ti + 0.5D * tx * tt;
        double ny = y + vy * ti + 0.5D * ty * tt;
        double nvx = vx + tx * ti;
        double nvy = vy + ty * ti;
        System.out.println("Updating Motion: \n " +
                "\t old State: Coords: ("+x +","+y +") velocity: ("+vx +","+vy +") \n" +
                "\t new State: Coords: ("+nx+","+ny+") velocity: ("+nvx+","+nvy+")");

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

        List<ObjectInstance> walls = s.objectsOfClass(CLASS_WALL);
        for(ObjectInstance wall: walls){
            double width = (double) wall.get(ATT_WIDTH);
            double height = (double) wall.get(ATT_HEIGHT);
            double wallX = (double) wall.get(ATT_START_X);
            double wallY = (double) wall.get(ATT_START_Y);
            double l = wallX;
            double r = wallX + width;
            double b = wallY;
            double t = wallY + height;

            if(height == 0){
                double slope = (ny-y)/(nx-x);
                double xint = y-(slope*x);
                double intercept =  (t-xint)/slope;
                if (intercept >= l && intercept <= r){
                    System.out.println("Collision at: ("+intercept+","+t+")");
                    nx = intercept;
                    ny = t;
                    nvx = 0.0D;
                    nvy = 0.0D;
                }
                break;
            }else if(width == 0){
                double slope = (ny-y)/(nx-x);
                double intercept = slope * l + (y-(slope*x));
                if (intercept >=b && intercept <=t){
                    System.out.println("Collision at: ("+l+","+intercept+")");
                    nx = l;
                    ny = intercept;
                    nvx = 0.0D;
                    nvy = 0.0D;
                }
                break;
            }


        }

        agent.set(ATT_X, nx);
        agent.set(ATT_Y, ny);
        agent.set(ATT_VX, nvx);
        agent.set(ATT_VY, nvy);
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
}
