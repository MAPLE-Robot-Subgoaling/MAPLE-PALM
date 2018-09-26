package edu.umbc.cs.maple.jumper;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import edu.umbc.cs.maple.jumper.state.JumperAgent;
import edu.umbc.cs.maple.jumper.state.JumperState;

import java.util.List;

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
            state = jump(state, 0.0, jumpRadius);
        } else if (actionName.equals(ACTION_SOUTH)) {
            state = jump(state, 0.0, -jumpRadius);
        } else if (actionName.equals(ACTION_EAST)) {
            state = jump(state, jumpRadius, 0.0);
        } else if (actionName.equals(ACTION_WEST)) {
            state = jump(state, -jumpRadius, 0.0);
        } else {
            throw new RuntimeException("Error: unknown action: " + actionName);
        }
        return state;
    }

    protected JumperState jump(JumperState state, double dX, double dY) {
        JumperAgent agent = state.touchAgent();
        double aX = (double) agent.get(ATT_X);
        double aY = (double) agent.get(ATT_Y);
        aX += dX;
        aY += dY;
        aX = Math.min(aX, maxX);
        aY = Math.min(aY, maxY);
        aX = Math.max(aX, minX);
        aY = Math.max(aY, minY);
        agent.set(ATT_X, aX);
        agent.set(ATT_Y, aY);
        return state;
    }

}
