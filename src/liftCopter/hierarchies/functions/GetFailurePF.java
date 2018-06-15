package liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import liftCopter.state.LiftCopterState;

import java.util.List;

import static liftCopter.LiftCopterConstants.*;
import static liftCopter.LiftCopterConstants.ATT_START_X;
import static liftCopter.LiftCopterConstants.ATT_START_Y;
import static taxi.TaxiConstants.CLASS_PASSENGER;

public class GetFailurePF extends PropositionalFunction {
    //get fails if any passenger if in taxi unless it is the right one

    public GetFailurePF() {
        super("getFail", new String[]{CLASS_PASSENGER});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        LiftCopterState state = (LiftCopterState) s;
        List<ObjectInstance> walls = state.objectsOfClass(CLASS_WALL);
        double ax = (double) state.getCopter().get(ATT_X);
        double ay = (double) state.getCopter().get(ATT_Y);
        double ah = (double) state.getCopter().get(ATT_H);
        double aw = (double) state.getCopter().get(ATT_W);
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
