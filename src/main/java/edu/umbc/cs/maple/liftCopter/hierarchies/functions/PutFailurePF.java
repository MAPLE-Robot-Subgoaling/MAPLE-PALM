package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.state.LCPutState;
import edu.umbc.cs.maple.liftCopter.state.LiftCopterState;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class PutFailurePF extends PropositionalFunction{
    //put fail if taxi is empty

    public PutFailurePF() {
        super("put", new String[]{CLASS_CARGO});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
//        LiftCopterState state = (LiftCopterState) s;
//        List<ObjectInstance> walls = state.objectsOfClass(CLASS_WALL);
//        double ax = (double) state.getCopter().get(ATT_X);
//        double ay = (double) state.getCopter().get(ATT_Y);
//        double ah = (double) state.getCopter().get(ATT_H);
//        double aw = (double) state.getCopter().get(ATT_W);
//        for (ObjectInstance wall : walls) {
//            double ww = (double) wall.get(ATT_WIDTH);
//            double wh = (double) wall.get(ATT_HEIGHT);
//            double wx = (double) wall.get(ATT_START_X);
//            double wy = (double) wall.get(ATT_START_Y);
////            System.out.println("Compare: \n" +
////                    "\t a:"+ax+","+ay+","+ah+","+aw+"\n" +
////                    "\t w:"+wall.name() + ","+wx+","+wy+","+wh+","+ww
////            );
//            if (wx < ax + aw &&
//                    wx + ww > ax &&
//                    wy < ay + ah &&
//                    wy + wh > ay) {
//                //System.out.println("Crashed into "+wall.name());
//                return true;
//            }
//
//        }
        if (!(s instanceof LCPutState)) { return false; }

        List<ObjectInstance> agents = s.objectsOfClass(CLASS_AGENT);
        if (agents.size() < 1) { return false; }
        ObjectInstance agent = agents.get(0);
        if (agent.get(ATT_LOCATION).equals(ATT_VAL_CRASHED)) { return true; }

        String cargoName = params[0];
        MutableObject cargo = (MutableObject) s.object(cargoName);
        if (cargo == null) { return false; }
        String cargoLocation = (String) cargo.get(ATT_LOCATION);
        return !cargoLocation.equals(ATT_VAL_PICKED_UP);
    }

}
