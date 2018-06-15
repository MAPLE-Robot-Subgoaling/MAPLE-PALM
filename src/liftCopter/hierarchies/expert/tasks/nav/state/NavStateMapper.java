package liftCopter.hierarchies.expert.tasks.nav.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import liftCopter.state.LiftCopterState;

import java.util.ArrayList;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;

public class NavStateMapper implements StateMapping {

    @Override
    public LCNavState mapState(State s) {
        if(s instanceof LCNavState) {
            return (LCNavState)s;
        }
        LiftCopterState st = (LiftCopterState) s;

        double ax = (double) st.getCopter().get(ATT_X);
        double ay = (double) st.getCopter().get(ATT_Y);
        double avx = (double) st.getCopter().get(ATT_VX);
        double avy = (double) st.getCopter().get(ATT_VY);
        double h = (double) st.getCopter().get(ATT_H);
        double w = (double) st.getCopter().get(ATT_W);

        LCNavAgent agent = new LCNavAgent(CLASS_AGENT, ax, ay, avx, avy, h, w);

        List<LCNavLocation> locations = new ArrayList<>();
        for(ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)){
            double lx = (double) location.get(ATT_X);
            double ly = (double) location.get(ATT_Y);
            double lh = (double) location.get(ATT_H);
            double lw = (double) location.get(ATT_W);

            locations.add(new LCNavLocation(location.name(), lx, ly, lh, lw));
        }
        List<LCNavWall> walls = new ArrayList<>();
        for(ObjectInstance wall : st.objectsOfClass(CLASS_WALL)) {
            double wx = (double) wall.get(ATT_START_X);
            double wy = (double) wall.get(ATT_START_Y);
            double wh = (double) wall.get(ATT_HEIGHT);
            double ww = (double) wall.get(ATT_WIDTH);
            walls.add(new LCNavWall(wall.name(), wx, wy, wh, ww));
        }

        return new LCNavState(agent, locations, walls);
    }
}

