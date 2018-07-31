package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.LCNavState;

import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class NavFailurePF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavFailurePF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCNavState)) { return false; }

        LCNavState state = (LCNavState) s;
        List<ObjectInstance> walls = state.objectsOfClass(CLASS_WALL);
        double ax = (double) state.touchCopter().get(ATT_X);
        double ay = (double) state.touchCopter().get(ATT_Y);
        double ah = (double) state.touchCopter().get(ATT_H);
        double aw = (double) state.touchCopter().get(ATT_W);
        for (ObjectInstance wall : walls) {
            double ww = (double) wall.get(ATT_WIDTH);
            double wh = (double) wall.get(ATT_HEIGHT);
            double wx = (double) wall.get(ATT_START_X);
            double wy = (double) wall.get(ATT_START_Y);
//            System.out.println("Compare: \n" +
//                    "\t a:"+ax+","+ay+","+ah+","+aw+"\n" +
//                    "\t w:"+wall.name() + ","+wx+","+wy+","+wh+","+ww
//            );
            if (wx < ax + aw &&
                    wx + ww > ax &&
                    wy < ay + ah &&
                    wy + wh > ay) {
                //System.out.println("Crashed into "+wall.name());
                return true;
            }

        }
        return false;
    }
}
