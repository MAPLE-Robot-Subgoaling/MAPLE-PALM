package taxi.hierarchies.tasks.nav.state;

import burlap.mdp.auxiliary.StateMapping;
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

        int tx = (int) st.getTaxiAtt(ATT_X);
        int ty = (int) st.getTaxiAtt(ATT_Y);
        TaxiNavAgent taxi = new TaxiNavAgent(CLASS_TAXI, tx, ty);

        List<TaxiNavLocation> locations = new ArrayList<>();
        for(String locName : st.getLocations()){
            int lx = (int) st.getLocationAtt(locName, ATT_X);
            int ly = (int) st.getLocationAtt(locName, ATT_Y);
            locations.add(new TaxiNavLocation(locName, lx, ly));
        }
        List<TaxiNavWall> walls = new ArrayList<>();
        for(String wallName : st.getWalls()){
            int startX = (int) st.getWallAtt(wallName, ATT_START_X);
            int startY = (int) st.getWallAtt(wallName, ATT_START_Y);
            int length = (int) st.getWallAtt(wallName, ATT_LENGTH);
            boolean isHorizontal = (boolean) st.getWallAtt(wallName, ATT_IS_HORIZONTAL);
            walls.add(new TaxiNavWall(wallName, startX, startY, length, isHorizontal));
        }

        return new TaxiNavState(taxi, locations, walls);
    }
}

