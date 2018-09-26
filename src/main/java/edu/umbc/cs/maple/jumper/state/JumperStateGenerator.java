package edu.umbc.cs.maple.jumper.state;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.utilities.BurlapConstants;
import edu.umbc.cs.maple.utilities.MathCommon;

import java.util.Random;

import static edu.umbc.cs.maple.jumper.JumperConstants.CLASS_AGENT;
import static edu.umbc.cs.maple.jumper.JumperConstants.CLASS_TARGET;

public class JumperStateGenerator implements StateGenerator {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double jumpRadius;

    public JumperStateGenerator(double minX, double maxX, double minY, double maxY, double jumpRadius) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.jumpRadius = jumpRadius;
    }

    @Override
    public State generateState() {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        double aX = MathCommon.nextDoubleInRange(rng, minX, maxX);
        double aY = MathCommon.nextDoubleInRange(rng, minY, maxY);
        double tX, tY;
        do {
            tX = MathCommon.nextDoubleInRange(rng, minX, maxX);
            tY = MathCommon.nextDoubleInRange(rng, minY, maxY);
        } while (MathCommon.distance(aX, aY, tX, tY) <= jumpRadius);


        JumperAgent agent = new JumperAgent(CLASS_AGENT + "0", aX, aY);
        JumperTarget target = new JumperTarget(CLASS_TARGET + "0", tX, tY);
        JumperState state = new JumperState(agent, target);
        return state;
    }
}
