package edu.umbc.cs.maple.jumper;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.jumper.state.JumperAgent;
import edu.umbc.cs.maple.jumper.state.JumperState;
import edu.umbc.cs.maple.utilities.BurlapConstants;
import edu.umbc.cs.maple.utilities.MathCommon;

import java.util.List;
import java.util.Random;

import static edu.umbc.cs.maple.jumper.JumperConstants.*;

public class JumperModel implements FullStateModel {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double jumpRadius;

    public JumperModel(double minX, double maxX, double minY, double maxY, double jumpRadius) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.jumpRadius = jumpRadius;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State state, Action action) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public State sample(State s, Action a) {
        JumperState state = (JumperState) s.copy();
        String actionName = a.actionName();
        if (actionName.equals(ACTION_NORTH)) {
            state = jump(state, 0, 1);
        } else if (actionName.equals(ACTION_SOUTH)) {
            state = jump(state, 0, -1);
        } else if (actionName.equals(ACTION_EAST)) {
            state = jump(state, 1, 0);
        } else if (actionName.equals(ACTION_WEST)) {
            state = jump(state, -1, 0);
        } else {
            throw new RuntimeException("Error: unknown action: " + actionName);
        }
        return state;
    }

    protected JumperState jump(JumperState state, int mX, int mY) {


        JumperAgent agent = state.touchAgent();
        double aX = (double) agent.get(ATT_X);
        double aY = (double) agent.get(ATT_Y);

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);
        // convert to a theta in the valid quarter arc region on unit circle
        double piOverFour = Math.PI / 4.0;
        double rollTheta;
        if (mX == 1 && mY == 0) {
            // 0 degrees, going East
            rollTheta = MathCommon.nextDoubleInRange(rng, -piOverFour, piOverFour);
        } else if (mX == 0 && mY == 1) {
            // 90 degrees, going North
            rollTheta = MathCommon.nextDoubleInRange(rng, piOverFour, 3*piOverFour);
        } else if (mX == -1 && mY == 0) {
            // 180 degrees, going West
            rollTheta = MathCommon.nextDoubleInRange(rng, 3*piOverFour, 5*piOverFour);
        } else if (mX == 0 && mY == -1) {
            // 270 degrees, going South
            rollTheta = MathCommon.nextDoubleInRange(rng, 5*piOverFour, 7*piOverFour);
        } else {
            throw new RuntimeException("Error: invalid position on unit circle");
        }
        double rollRadius = rng.nextDouble();
        double radius = jumpRadius*Math.sqrt(rollRadius);
        double theta = rollTheta;// * 2 * Math.PI;
        double dX = radius * Math.cos(theta);
        double dY = radius * Math.sin(theta);

        aX += dX;
        aY += dY;
//        aX = Math.min(aX, maxX);
        if (aX >= maxX) {
            aX = maxX - (aX - maxX);
        }
//        aY = Math.min(aY, maxY);
        if (aY >= maxY) {
            aY = maxY - (aY - maxY);
        }
//        aX = Math.max(aX, minX);
        if (aX <= minX) {
            aX = minX - (aX - minX);
        }
//        aY = Math.max(aY, minY);
        if (aY <= minY) {
            aY = minY - (aY - minY);
        }
        agent.set(ATT_X, aX);
        agent.set(ATT_Y, aY);
        return state;
    }

}
