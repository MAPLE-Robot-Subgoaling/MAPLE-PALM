package liftCopter;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import liftCopter.state.LiftCopterState;

import static liftCopter.LiftCopterConstants.*;

public class LiftCopterTerminalFunction implements TerminalFunction {
    //the liftCopter domain is terminal when all cargos are at their goal
    //and have been picked up and not in the liftCopter anymore

    @Override
    public boolean isTerminal(State s) {
        LiftCopterState state = (LiftCopterState) s;

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
                    if (!(lx + lw >= px+pw/2 && lx <= px+pw/2 && ly + lh >= py+ph/2 && ly <= py+ph/2)) {
                        return false;
                    }
                    break;
                }
            }
        }
        return true;
    }
}
