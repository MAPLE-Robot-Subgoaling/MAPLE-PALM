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

public class RandomPassengerTaxiState implements StateGenerator{

	@Override
	public State generateState() {
		int width = 5;
		int height = 5;
		
		int tx = (int) (RandomFactory.getMapped(0).nextDouble() * width);
		int ty = (int) (RandomFactory.getMapped(0).nextDouble() * height);
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, tx, ty);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 4, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 0, Taxi.COLOR_YELLOW));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 2, 3, 0, Taxi.COLOR_BLUE));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 3, 4, 4, Taxi.COLOR_GREEN));
		
		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		
		int start = (int)(RandomFactory.getMapped(0).nextDouble() * 4);
		int px = (int)(locations.get(start).get(Taxi.ATT_X));
		int py = (int)(locations.get(start).get(Taxi.ATT_Y));
		int goal;
		do
			goal = (int)(RandomFactory.getMapped(0).nextDouble() * 4);
		while(goal==start);

		String goalName =  locations.get(goal).name();
		
		passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, px, py, goalName));
		
		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 0, 0, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 1, 0, 0, 5, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 2, 5, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 3, 0, 5, 5, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 4, 1, 0, 2, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 5, 3, 0, 2, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 6, 2, 3, 2, false));
		
		return new TaxiState(taxi, passengers, locations, walls);
	}

}
