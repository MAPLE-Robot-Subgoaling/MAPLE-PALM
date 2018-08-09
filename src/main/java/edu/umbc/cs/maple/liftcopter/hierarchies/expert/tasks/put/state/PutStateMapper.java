package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.put.state;

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

public class PutStateMapper implements ParameterizedStateMapping {

//    public static final String PUT_CARGO_ALIAS = "**PUT_CARGO_ALIAS**";

    //maps a base agent state to L2
    @Override
    public State mapState(State s, String... params) {
        List<LCPutCargo> cargos = new ArrayList<LCPutCargo>();
        List<LCPutLocation> locations = new ArrayList<>();

        LiftCopterState st = (LiftCopterState) s;

        // Get Agent
        String agentLocation = ATT_VAL_IN_AIR;
        double ax = (double)st.getCopter().get(ATT_X);
        double ay = (double)st.getCopter().get(ATT_Y);
        double ah = (double)st.getCopter().get(ATT_H);
        double aw = (double)st.getCopter().get(ATT_W);
        List<ObjectInstance> walls = st.objectsOfClass(CLASS_WALL);


        for (ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)) {
            double lx = (double) location.get(ATT_X);
            double ly = (double) location.get(ATT_Y);
            double lh = (double) location.get(ATT_H);
            double lw = (double) location.get(ATT_W);

            locations.add(new LCPutLocation(location.name()));

            if (lx < ax + aw &&
                    lx + lw > ax &&
                    ly < ay + ah &&
                    ly + lh > ay) {
                agentLocation = location.name();
            }
        }
        agentLocation = LiftCopter.collidedWithWall((OOState)s) ? ATT_VAL_CRASHED : agentLocation;
        LCPutAgent agent = new LCPutAgent(CLASS_AGENT, agentLocation);
        for (ObjectInstance wall : walls) {
            double ww = (double) wall.get(ATT_WIDTH);
            double wh = (double) wall.get(ATT_HEIGHT);
            double wx = (double) wall.get(ATT_START_X);
            double wy = (double) wall.get(ATT_START_Y);

            if (wx < ax + aw &&
                    wx + ww > ax &&
                    wy < ay + ah &&
                    wy + wh > ay) {
                agent.set(ATT_LOCATION, ATT_VAL_CRASHED);
            }
        }
        for(String cargoName : params){
            LiftCopterCargo cargo = (LiftCopterCargo) st.object(cargoName);
            String goal = (String) cargo.get(ATT_GOAL_LOCATION);
            boolean inAgent = (boolean) cargo.get(ATT_PICKED_UP);
            String location = ERROR;
            if (inAgent) {
                location = ATT_VAL_PICKED_UP;
            } else {
                double px = (double)cargo.get(ATT_X);
                double py = (double)cargo.get(ATT_Y);
                double ph = (double)cargo.get(ATT_H);
                double pw = (double)cargo.get(ATT_W);

                for (ObjectInstance otherLocation : st.objectsOfClass(CLASS_LOCATION)) {
                    double lx = (double) otherLocation.get(ATT_X);
                    double ly = (double) otherLocation.get(ATT_Y);
                    double lh = (double) otherLocation.get(ATT_H);
                    double lw = (double) otherLocation.get(ATT_W);
                    if (lx < px + pw &&
                            lx + lw > px &&
                            ly < py + ph &&
                            ly + lh > py) {
                        location = otherLocation.name();
                    }
                }
            }
            if (location.equals(ERROR)) { throw new RuntimeException("Error: cargo at invalid location in mapper"); }
//            cargos.add(new AgentPutCargo(PUT_CARGO_ALIAS, goal, location));
            cargos.add(new LCPutCargo(cargoName, goal, location));
        }

        return new LCPutState(agent, cargos, locations);
    }

}
