package liftCopter.hierarchies.expert.tasks.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import liftCopter.state.LiftCopterState;

import java.util.ArrayList;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;

public class RootStateMapper implements StateMapping {
    @Override
    public State mapState(State s) {
        List<LCRootCargo> cargos = new ArrayList<>();
        LiftCopterState st = (LiftCopterState) s;

        for(ObjectInstance cargo : st.objectsOfClass(CLASS_CARGO)){
            double cx = (double) cargo.get(ATT_X);
            double cy = (double) cargo.get(ATT_Y);
            double ch = (double) cargo.get(ATT_H);
            double cw = (double) cargo.get(ATT_W);

            String goalLocation = (String) cargo.get(ATT_GOAL_LOCATION);
            boolean inTaxi = (boolean) cargo.get(ATT_PICKED_UP);

            if(inTaxi) {
                cargos.add(new LCRootCargo(cargo.name(), ATT_VAL_PICKED_UP, goalLocation));
            } else {
                for(ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)){
                    double lx = (double) location.get(ATT_X);
                    double ly = (double) location.get(ATT_Y);
                    double lh = (double) location.get(ATT_H);
                    double lw = (double) location.get(ATT_W);

                    if (cx < lx + lw &&
                            cx + cw > lx &&
                            cy < ly + lh &&
                            cy + ch > ly) {
                        cargos.add(new LCRootCargo(cargo.name(), location.name(), goalLocation));
                    }
                }
            }
        }

        return new LCRootState(cargos);
    }

}
