package taxi.stateGenerator;

import java.util.ArrayList;
import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;
import taxi.state.TaxiWall;

public class RandomPassengerSmallTaxiState implements StateGenerator{

	@Override
	public State generateState() {
		int width = 0;
		int height = 5;

		int tx = (int) (Math.random() * width);
		int ty = (int) (Math.random() * height);
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, tx, ty);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 2, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 0, Taxi.COLOR_YELLOW));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 2, 0, 1, Taxi.COLOR_BLUE));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 3, 0, 3, Taxi.COLOR_GREEN));
	
		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();

		int start = (int)(Math.random() * 4);
		int px = (int)(locations.get(start).get(Taxi.ATT_X));
		int py = (int)(locations.get(start).get(Taxi.ATT_Y));
		int goal = (int)(Math.random() * 4);
		String goalName =  locations.get(goal).name();
		
		passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, px, py, goalName));
		
		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 0, 0, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 1, 1, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 2, 0, 0, 1, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 3, 0, 5, 1, true));
			
		return new TaxiState(taxi, passengers, locations, walls);
	}

}
