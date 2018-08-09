package edu.umbc.cs.maple.liftcopter;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterState;

import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class LiftCopterRewardFunction implements RewardFunction {

    /**
     * the rewardTotal for taking a action
     */
    private double stepReward;

    /**
     * the rewardTotal for a impossible pickup or dropoff action
     */
    private double illegalActionReward;

    /**
     * the rewardTotal for completing the goal
     */
    private double goalReward;

    /**
     * the liftcopter terminal function
     */
    private TerminalFunction tf;

    /**
     * use the default rewards
     */
    public LiftCopterRewardFunction() {
        // scaled to fit goal at 1.0
        // modified to reflect # actions in liftcopter vs taxi
        stepReward = -0.0005;//-1;
        illegalActionReward = -0.005;//-10;
        goalReward = 1.0;//20;
        tf = new LiftCopterTerminalFunction();
    }

    /**
     * use custom rewards
     *
     * @param stepR    the rewardTotal for a action
     * @param illegalR the rewardTotal for a impossible pickup or dropoff
     * @param goalR    the rewardTotal for completing the goal
     */
    public LiftCopterRewardFunction(double stepR, double illegalR, double goalR) {
        stepReward = stepR;
        illegalActionReward = illegalR;
        goalReward = goalR;
        tf = new LiftCopterTerminalFunction();
    }

    @Override
    public double reward(State s, Action a, State sprime) {
        LiftCopterState state = (LiftCopterState) s;

        //goal rewardTotal when state is terminal
        if (tf.isTerminal(sprime)){
            LiftCopterState sp = (LiftCopterState) sprime;
            boolean isCrash = false;
            List<ObjectInstance> walls = sp.objectsOfClass(CLASS_WALL);
            double ax = (double) sp.getCopter().get(ATT_X);
            double ay = (double) sp.getCopter().get(ATT_Y);
            double ah = (double) sp.getCopter().get(ATT_H);
            double aw = (double) sp.getCopter().get(ATT_W);
            for(ObjectInstance wall: walls){
                double ww = (double) wall.get(ATT_WIDTH);
                double wh = (double) wall.get(ATT_HEIGHT);
                double wx = (double) wall.get(ATT_START_X);
                double wy = (double) wall.get(ATT_START_Y);

                if (    wx < ax + aw &&
                        wx + ww > ax &&
                        wy < ay + ah &&
                        wy + wh > ay) {
//                   System.out.println("crash");
                    isCrash = true;
                    break;
                }
            }
            if(!isCrash){
               // System.out.println("goal");
                return goalReward + stepReward;
            }else{
                return stepReward-goalReward;
            }

        }


//		boolean LiftCopterOccupied = (boolean) state.getLiftCopterAtt(ATT_LiftCopter_OCCUPIED);
        boolean LiftCopterOccupied = state.isLiftCopterOccupied();
        double tx = (double) state.getCopter().get(ATT_X);
        double ty = (double) state.getCopter().get(ATT_Y);

        //illegal pickup when no cargo at liftcopter's location
        if (a.actionName().equals(ACTION_PICKUP)) {

            boolean cargoAtLiftCopter = false;
            for (ObjectInstance cargo : state.objectsOfClass(CLASS_CARGO)) {
                double px = (double) cargo.get(ATT_X);
                double py = (double) cargo.get(ATT_Y);
                double ph = (double) cargo.get(ATT_H);
                double pw = (double) cargo.get(ATT_W);
                boolean inLiftCopter = (boolean) cargo.get(ATT_PICKED_UP);
                if (px + pw/2 >= tx &&
                        px - pw/2 <= tx &&
                        py + ph/2 >= ty &&
                        py - ph/2 <= ty) {
                    cargoAtLiftCopter = true;
                    break;
                }
            }

            if (!cargoAtLiftCopter)
                return stepReward + illegalActionReward;
        }
        //illegal dropoff if not at depot or cargo not in liftcopter
        else if (a.actionName().startsWith(ACTION_PUTDOWN)) {
            if (!LiftCopterOccupied)
                return stepReward + illegalActionReward;

            // if liftcopter/cargo is not at depot
            boolean LiftCopterAtDepot = false;
            for (ObjectInstance location : state.objectsOfClass(CLASS_LOCATION)) {
                double lx = (double) location.get(ATT_X);
                double ly = (double) location.get(ATT_Y);
                double lh = (double) location.get(ATT_H);
                double lw = (double) location.get(ATT_W);
                if (lx + lw/2 >= tx &&
                        lx - lw/2 <= tx &&
                        ly + lh/2 >= ty &&
                        ly - lh/2 <= ty) {
                    LiftCopterAtDepot = true;
                    break;
                }
            }

            if (!LiftCopterAtDepot)
                return stepReward + illegalActionReward;
        }

        return stepReward;
    }
}
