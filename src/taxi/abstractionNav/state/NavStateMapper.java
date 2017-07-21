package taxi.abstractionNav.state;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

public class NavStateMapper implements StateMapping {

	@Override
	public State mapState(State s) {
		TaxiState st = (TaxiState) s;
		
		String tName = st.getTaxiName();
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		TaxiNavAgent taxi = new TaxiNavAgent(tName, tx, ty);
		
		List<TaxiNavLocation> locations = new ArrayList<TaxiNavLocation>();
		for(String locName : st.getLocations()){
			int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
			int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);
			locations.add(new TaxiNavLocation(locName, lx, ly));
		}
		
		List<TaxiNavWall> walls = new ArrayList<TaxiNavWall>();
		for(String wallName : st.getWalls()){
			int startX = (int) st.getWallAtt(wallName, Taxi.ATT_START_X);
			int startY = (int) st.getWallAtt(wallName, Taxi.ATT_START_Y);
			int length = (int) st.getWallAtt(wallName, Taxi.ATT_LENGTH);
			boolean isHorizontal = (boolean) st.getWallAtt(wallName, Taxi.ATT_IS_HORIZONTAL);
			walls.add(new TaxiNavWall(wallName, startX, startY, length, isHorizontal));
		}
		
		return new TaxiNavState(taxi, locations, walls);
	}

}