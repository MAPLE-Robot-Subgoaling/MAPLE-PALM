package taxi.hierarchies.tasks.nav.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class NavStateMapper implements StateMapping {

    @Override
    public TaxiNavState mapState(State s) {
        if(s instanceof TaxiNavState) {
            return (TaxiNavState)s;
        }
        TaxiState st = (TaxiState) s;

        int tx = (int) st.getTaxi().get(ATT_X);
        int ty = (int) st.getTaxi().get(ATT_Y);
        TaxiNavAgent taxi = new TaxiNavAgent(CLASS_TAXI, tx, ty);

        List<TaxiNavLocation> locations = new ArrayList<>();
        for(ObjectInstance location : st.objectsOfClass(CLASS_LOCATION)){
            int lx = (int) location.get(ATT_X);
            int ly = (int) location.get(ATT_Y);
            locations.add(new TaxiNavLocation(location.name(), lx, ly));
        }
        List<TaxiNavWall> walls = new ArrayList<>();
        for(ObjectInstance wall : st.objectsOfClass(CLASS_WALL)) {
            int startX = (int) wall.get(ATT_START_X);
            int startY = (int) wall.get(ATT_START_Y);
            int length = (int) wall.get(ATT_LENGTH);
            boolean isHorizontal = (boolean) wall.get(ATT_IS_HORIZONTAL);
            walls.add(new TaxiNavWall(wall.name(), startX, startY, length, isHorizontal));
        }

        return new TaxiNavState(taxi, locations, walls);
    }
}

