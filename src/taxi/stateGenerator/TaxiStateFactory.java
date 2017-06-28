
package taxi.stateGenerator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import sun.util.logging.resources.logging;
import taxi.Taxi;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;
import taxi.state.TaxiWall;

public class TaxiStateFactory {
	//generates taxi states
	
	public static TaxiState createClassicState(){
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, 0, 3);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 4, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 0, Taxi.COLOR_YELLOW));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 2, 3, 0, Taxi.COLOR_BLUE));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 3, 4, 4, Taxi.COLOR_GREEN));
		
		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, 3, 0, Taxi.CLASS_LOCATION + 0));
		passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 1, 0, 0, Taxi.CLASS_LOCATION + 3));
		
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
	
	public static TaxiState createSmallState(){
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, 0, 4);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 2, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 0, Taxi.COLOR_YELLOW));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 2, 0, 1, Taxi.COLOR_BLUE));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 3, 0, 3, Taxi.COLOR_GREEN));
	
		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, 0, 1, Taxi.CLASS_LOCATION + 0));
		
		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 0, 0, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 1, 1, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 2, 0, 0, 1, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 3, 0, 5, 1, true));
			
		return new TaxiState(taxi, passengers, locations, walls);
	}
	
	public static TaxiState createTinyState(){
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, 0, 1);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 0, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 1, Taxi.COLOR_BLUE));
		
		List<TaxiPassenger> passenger = new ArrayList<TaxiPassenger>();
		passenger.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, 0, 0, Taxi.CLASS_LOCATION + 1));
		
		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 0, 0, 0, 1, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 1, 0, 0, 2, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 2, 0, 2, 1, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 3, 1, 0, 2, false));
		
		return new TaxiState(taxi, passenger, locations, walls);
	}
}
