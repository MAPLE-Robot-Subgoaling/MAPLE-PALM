package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.state;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.liftcopter.LiftCopter;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterCargo;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterState;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.ParameterizedStateMapping;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class GetStateMapper implements ParameterizedStateMapping {

//    public static final String GET_cargo_ALIAS = "**GET_cargo_ALIAS**";

    //maps a base agent state to L2
    @Override
    public State mapState(State s, String...params) {
        List<LCGetCargo> cargos = new ArrayList<LCGetCargo>();
        List<LCGetLocation> locations = new ArrayList<LCGetLocation>();
        LiftCopterState st = (LiftCopterState) s;

        // Get agent
        String agentLocation = ATT_VAL_IN_AIR;
        double ax = (double)st.getCopter().get(ATT_X);
        double ay = (double)st.getCopter().get(ATT_Y);
        double ah = (double)st.getCopter().get(ATT_H);
        double aw = (double)st.getCopter().get(ATT_W);
        for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
            double lx = (double) location.get(ATT_X);
            double ly = (double) location.get(ATT_Y);
            double lh = (double) location.get(ATT_H);
            double lw = (double) location.get(ATT_W);


            locations.add(new LCGetLocation(location.name()));

            if (lx < ax + aw &&
                    lx + lw > ax &&
                    ly < ay + ah &&
                    ly + lh > ay) {
                agentLocation = location.name();
            }
        }
        agentLocation = LiftCopter.collidedWithWall((OOState)s) ? ATT_VAL_CRASHED : agentLocation;
        LCGetAgent agent = new LCGetAgent(CLASS_AGENT, agentLocation);

        // Get cargos
        for(String cargoName : params){
            LiftCopterCargo cargo = (LiftCopterCargo) st.object(cargoName);
            double px = (double)cargo.get(ATT_X);
            double py = (double)cargo.get(ATT_Y);
            double ph = (double)cargo.get(ATT_H);
            double pw = (double)cargo.get(ATT_W);

            boolean inagent = (boolean) cargo.get(ATT_PICKED_UP);
            String cargoLocation = ATT_VAL_PICKED_UP;

            if(!inagent) {
                for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
                    double lx = (double) location.get(ATT_X);
                    double ly = (double) location.get(ATT_Y);
                    double lh = (double) location.get(ATT_H);
                    double lw = (double) location.get(ATT_W);

                    if (lx < px + pw &&
                            lx + lw > px &&
                            ly < py + ph &&
                            ly + lh > py) {
                        cargoLocation = location.name();
                    }
                }
            }
            cargos.add(new LCGetCargo(cargoName, cargoLocation));
//            cargos.add(new agentGetcargo(GET_cargo_ALIAS, cargoLocation));
        }
        LCGetState getState = new LCGetState(agent, cargos, locations);
        LiftCopterState state = (LiftCopterState) s;
        List<ObjectInstance> walls = state.objectsOfClass(CLASS_WALL);
        for (ObjectInstance wall : walls) {
            double ww = (double) wall.get(ATT_WIDTH);
            double wh = (double) wall.get(ATT_HEIGHT);
            double wx = (double) wall.get(ATT_START_X);
            double wy = (double) wall.get(ATT_START_Y);

            if (wx < ax + aw &&
                    wx + ww > ax &&
                    wy < ay + ah &&
                    wy + wh > ay) {
                getState.hasFailed = true;
                getState.touchAgent().set(ATT_LOCATION, ATT_VAL_CRASHED);
            }

        }
        return getState;
    }

}
