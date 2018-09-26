package edu.umbc.cs.maple.jumper;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.jumper.state.JumperAgent;
import edu.umbc.cs.maple.jumper.state.JumperState;
import edu.umbc.cs.maple.jumper.state.JumperTarget;
import edu.umbc.cs.maple.utilities.MathCommon;

import java.util.Map;

import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_X;
import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_Y;
import static edu.umbc.cs.maple.utilities.BurlapConstants.EMPTY_ARRAY;

public class AgentNearAnyTargetPF extends PropositionalFunction {

    private static final String NAME = "AgentNearAnyTargetPF";

    protected double radius;

    public AgentNearAnyTargetPF(double radius) {
        super(NAME, EMPTY_ARRAY);
        this.radius = radius;
    }

    @Override
    public boolean isTrue(OOState s, String... strings) {
        JumperState state = (JumperState) s;
        JumperAgent agent = state.getAgent();
        double aX = (double) agent.get(ATT_X);
        double aY = (double) agent.get(ATT_Y);
        Map<String, JumperTarget> targets = state.getTargets();
        for(Map.Entry<String, JumperTarget> entry : targets.entrySet()) {
            JumperTarget target = entry.getValue();
            double tX = (double) target.get(ATT_X);
            double tY = (double) target.get(ATT_Y);
            double distance = MathCommon.distance(aX, aY, tX, tY);
            if (distance <= radius) {
                // any true
                return true;
            }
        }
        return false;
    }

}
