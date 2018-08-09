package edu.umbc.cs.maple.liftcopter;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterState;

import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class LiftCopterTerminalFunction implements TerminalFunction {
    //the liftcopter domain is terminal when all cargos are at their goal
    //and have been picked up and not in the liftcopter anymore

    @Override
    public boolean isTerminal(State s) {
        LiftCopterState state = (LiftCopterState) s;
        if (LiftCopter.collidedWithWall(state)) { return true; }

        for (ObjectInstance cargo : state.objectsOfClass(CLASS_CARGO)) {
            double px = (double) cargo.get(ATT_X);
            double py = (double) cargo.get(ATT_Y);
            double ph = (double) cargo.get(ATT_H);
            double pw = (double) cargo.get(ATT_W);
            boolean inLiftCopter = (boolean) cargo.get(ATT_PICKED_UP);
            if (inLiftCopter)
                return false;

            String cargoGoal = (String) cargo.get(ATT_GOAL_LOCATION);

            for (ObjectInstance location : state.objectsOfClass(CLASS_LOCATION)) {
                double lx = (double) location.get(ATT_X);
                double ly = (double) location.get(ATT_Y);
                double lh = (double) location.get(ATT_H);
                double lw = (double) location.get(ATT_W);
                if (cargoGoal.equals(location.name())) {
                    if (!(lx - lw/2 <= px &&
                            lx + lw/2 >= px &&
                            ly - lh/2 <= py &&
                            ly + lh/2 >= py) ) {
                        return false;
                    }
                    break;
                }
            }

        }
        return true;
    }
}
