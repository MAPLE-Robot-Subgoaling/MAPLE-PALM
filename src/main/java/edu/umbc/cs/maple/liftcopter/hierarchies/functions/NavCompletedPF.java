package edu.umbc.cs.maple.liftcopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.nav.state.LCNavState;

import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class NavCompletedPF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavCompletedPF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {

        List<ObjectInstance> agents = s.objectsOfClass(CLASS_AGENT);
        if (agents.size() < 1) {
            return false;
        }
        ObjectInstance agent = agents.get(0);
        String locationName = params[0];
        ObjectInstance depot = s.object(locationName);
        double agentX = (double) agent.get(ATT_X);
        double agentY = (double) agent.get(ATT_Y);
        double agentW = (double) agent.get(ATT_W);
        double agentH = (double) agent.get(ATT_H);
        double agentMiddleX = agentX + agentW * 0.5;
        double agentMiddleY = agentY + agentH * 0.5;
        double depotX = (double) depot.get(ATT_X);
        double depotY = (double) depot.get(ATT_Y);
        double depotW = (double) depot.get(ATT_W);
        double depotH = (double) depot.get(ATT_H);
        boolean xInside = agentMiddleX >= depotX && agentMiddleX <= depotX + depotW;
        boolean yInside = agentMiddleY >= depotY && agentMiddleY <= depotY + depotH;
        return xInside && yInside;
    }

}
