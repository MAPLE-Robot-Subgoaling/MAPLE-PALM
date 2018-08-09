package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterState;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class RootStateMapper implements StateMapping {
    @Override
    public State mapState(State s) {
        LiftCopterState st = (LiftCopterState) s;
        List<LCRootCargo> cargos = new ArrayList<>();
        LCRootCopter copter = new LCRootCopter(st.getCopter().name(), (String) st.getCopter().get(ATT_LOCATION));
        String agentLocation = ATT_VAL_IN_AIR;
        double ax = (double) st.getCopter().get(ATT_X);
        double ay = (double) st.getCopter().get(ATT_Y);
        double ah = (double) st.getCopter().get(ATT_H);
        double aw = (double) st.getCopter().get(ATT_W);
        for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
            double lx = (double) location.get(ATT_X);
            double ly = (double) location.get(ATT_Y);
            double lh = (double) location.get(ATT_H);
            double lw = (double) location.get(ATT_W);

            if (ax < lx + lw &&
                    ax + aw > lx &&
                    ay < ly + lh &&
                    ay + ah > ly) {
                agentLocation = location.name();
            }
        }
        agentLocation = LiftCopter.collidedWithWall((OOState)s) ? ATT_VAL_CRASHED : agentLocation;
        copter.set(ATT_LOCATION, agentLocation);

        for (ObjectInstance cargo : st.objectsOfClass(CLASS_CARGO)) {
            double cx = (double) cargo.get(ATT_X);
            double cy = (double) cargo.get(ATT_Y);
            double ch = (double) cargo.get(ATT_H);
            double cw = (double) cargo.get(ATT_W);

            String goalLocation = (String) cargo.get(ATT_GOAL_LOCATION);
            boolean inTaxi = (boolean) cargo.get(ATT_PICKED_UP);

            if (inTaxi) {
                cargos.add(new LCRootCargo(cargo.name(), ATT_VAL_PICKED_UP, goalLocation));
            } else {
                for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
                    double lx = (double) location.get(ATT_X);
                    double ly = (double) location.get(ATT_Y);
                    double lh = (double) location.get(ATT_H);
                    double lw = (double) location.get(ATT_W);


        for (ObjectInstance cargo : st.objectsOfClass(CLASS_CARGO)) {
            double cx = (double) cargo.get(ATT_X);
            double cy = (double) cargo.get(ATT_Y);
            double ch = (double) cargo.get(ATT_H);
            double cw = (double) cargo.get(ATT_W);

            String goalLocation = (String) cargo.get(ATT_GOAL_LOCATION);
            boolean inTaxi = (boolean) cargo.get(ATT_PICKED_UP);

            if (inTaxi) {
                cargos.add(new LCRootCargo(cargo.name(), ATT_VAL_PICKED_UP, goalLocation));
            } else {
                for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
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

        List<ObjectInstance> walls = st.objectsOfClass(CLASS_WALL);

        for (ObjectInstance wall : walls) {
            double ww = (double) wall.get(ATT_WIDTH);
            double wh = (double) wall.get(ATT_HEIGHT);
            double wx = (double) wall.get(ATT_START_X);
            double wy = (double) wall.get(ATT_START_Y);

            if (wx < ax + aw &&
                    wx + ww > ax &&
                    wy < ay + ah &&
                    wy + wh > ay) {
                copter.set(ATT_LOCATION, ATT_VAL_CRASHED);
            }
        }
        LCRootState rootState = new LCRootState(cargos, copter);
        return rootState;
    }
}
