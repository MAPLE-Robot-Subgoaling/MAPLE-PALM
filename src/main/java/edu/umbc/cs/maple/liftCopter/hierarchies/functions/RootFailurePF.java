package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.LCRootState;
import edu.umbc.cs.maple.liftCopter.state.LiftCopterState;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.TaxiRootState;

import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class RootFailurePF extends PropositionalFunction {

    public RootFailurePF() {
        super("rootFail", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCRootState)) { return false; }
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
