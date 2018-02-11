
package taxi.stateGenerator;

import java.util.ArrayList;
import java.util.List;

import burlap.debugtools.RandomFactory;
import taxi.Taxi;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;
import taxi.state.TaxiWall;

public class TaxiStateFactory {
	//generates taxi states

    public static TaxiState createClassicState() {
        return createClassicState(1);
    }

	public static TaxiState createClassicState(int numPassengers){
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, 0, 3);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 4, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 0, Taxi.COLOR_YELLOW));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 2, 3, 0, Taxi.COLOR_BLUE));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 3, 4, 4, Taxi.COLOR_GREEN));

		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		for (int i = 0; i < numPassengers; i++){
            // classic taxi has original passenger at BLUE depot going to RED depot
		    int startX = 3;
		    int startY = 0;
		    String goalLocationName = Taxi.CLASS_LOCATION+0;
            // other passengers both start and go to a random depot
            if (i > 0) {
                // get a random goal
                goalLocationName = locations.get(RandomFactory.getMapped(0).nextInt(locations.size())).getName();
                String startLocationName = goalLocationName;
                while (startLocationName.equals(goalLocationName)) {
                    // put the agent in a random depot that is NOT the goal
                    TaxiLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
                    startX = (int) startLocation.get(Taxi.ATT_X);
                    startY = (int) startLocation.get(Taxi.ATT_Y);
                    startLocationName = startLocation.getName();
                } ;
            }
            passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + i, startX, startY, goalLocationName));
		}

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

	public static TaxiState createClassicStateHalfpoint(boolean inTaxi) {
		TaxiState classic = createClassicState();
		TaxiPassenger passenger = (TaxiPassenger) classic.objectsOfClass(Taxi.CLASS_PASSENGER).get(0);
		TaxiAgent agent = (TaxiAgent) classic.objectsOfClass(Taxi.CLASS_TAXI).get(0);
		agent.set(Taxi.ATT_X, passenger.get(Taxi.ATT_X));
		agent.set(Taxi.ATT_Y, passenger.get(Taxi.ATT_Y));
		agent.set(Taxi.ATT_TAXI_OCCUPIED, inTaxi);
		passenger.set(Taxi.ATT_IN_TAXI, inTaxi);
//		passenger.set(Taxi.ATT_JUST_PICKED_UP, true);
		return classic;
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
