
package taxi.stateGenerator;

import burlap.debugtools.RandomFactory;
import taxi.Taxi;
import taxi.TaxiConstants;
import taxi.state.*;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class TaxiStateFactory {
	//generates taxi states

    public static TaxiState createClassicState() {
        return createClassicState(1);
    }

	public static TaxiState createClassicState(int numPassengers){

		TaxiAgent taxi = new TaxiAgent(TaxiConstants.CLASS_TAXI + 0, 0, 12);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 0, 0, 19, TaxiConstants.COLOR_RED));
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 1, 0, 0, TaxiConstants.COLOR_YELLOW));
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 2, 12, 0, TaxiConstants.COLOR_BLUE));
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 3, 19, 19, TaxiConstants.COLOR_GREEN));

		/*----------------------------------------------------------------------------master
		TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 3);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 4, COLOR_RED));
		locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 0, COLOR_YELLOW));
		locations.add(new TaxiLocation(CLASS_LOCATION + 2, 3, 0, COLOR_BLUE));
		locations.add(new TaxiLocation(CLASS_LOCATION + 3, 4, 4, COLOR_GREEN));
		------------------------------------------------------------------------------*/

		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		for (int i = 0; i < numPassengers; i++){
            // classic taxi has original passenger at BLUE depot going to RED depot
		    int startX = 12;
		    int startY = 0;
		    String goalLocationName = CLASS_LOCATION+0;
            // other passengers both start and go to a random depot
            if (i > 0) {
//                 get a random goal
                goalLocationName = locations.get(RandomFactory.getMapped(0).nextInt(locations.size())).getName();
                String startLocationName = goalLocationName;
                while (startLocationName.equals(goalLocationName)) {
                    // put the agent in a random depot that is NOT the goal
                    TaxiLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
                    startX = (int) startLocation.get(ATT_X);
                    startY = (int) startLocation.get(ATT_Y);
                    startLocationName = startLocation.getName();
                }
//                startX = 3;
//                startY = 0;
//                goalLocationName = CLASS_LOCATION+0;
            }
            passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
		}

		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 0, 0, 0, 20, false)); //vertical left
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 1, 0, 0, 20, true)); //horizontal bottom
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 2, 20, 0, 20, false)); //vertical right
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 3, 0, 20, 20, true)); //horizontal top
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 4, 5, 0, 10, false));
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 5, 15, 0, 10, false));
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 6, 10, 10, 10, false));
		//additions to base map
		//walls.add(new TaxiWall(Taxi.CLASS_WALL + 7, 8, 6, 5, true ));
		//walls.add(new TaxiWall(Taxi.CLASS_WALL + 8, 16, 15, 5, true));
		//walls.add(new TaxiWall(Taxi.CLASS_WALL + 9, 0, 15, 5, true));
		//walls.add(new TaxiWall(Taxi.CLASS_WALL + 10, 8, 15, 5, true));
		
		return new TaxiState(taxi, passengers, locations, walls);
	}
	
	public static TaxiState createClassic20State() {
        return createClassicState(1);
    }
	
	public static TaxiState createClassic20State(int numPassengers){
		TaxiAgent taxi = new TaxiAgent(TaxiConstants.CLASS_TAXI + 0, 0, 12);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 0, 0, 19, TaxiConstants.COLOR_RED));
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 1, 0, 0, TaxiConstants.COLOR_YELLOW));
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 2, 12, 0, TaxiConstants.COLOR_BLUE));
		locations.add(new TaxiLocation(TaxiConstants.CLASS_LOCATION + 3, 19, 19, TaxiConstants.COLOR_GREEN));

		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		for (int i = 0; i < numPassengers; i++){
            // classic taxi has original passenger at BLUE depot going to RED depot
		    int startX = 12;
		    int startY = 0;
		    String goalLocationName = TaxiConstants.CLASS_LOCATION+0;
            // other passengers both start and go to a random depot
            if (i > 0) {
//                 get a random goal
                goalLocationName = locations.get(RandomFactory.getMapped(0).nextInt(locations.size())).getName();
                String startLocationName = goalLocationName;
                while (startLocationName.equals(goalLocationName)) {
                    // put the agent in a random depot that is NOT the goal
                    TaxiLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
                    startX = (int) startLocation.get(TaxiConstants.ATT_X);
                    startY = (int) startLocation.get(TaxiConstants.ATT_Y);
                    startLocationName = startLocation.getName();
                }
//                startX = 3;
//                startY = 0;
//                goalLocationName = Taxi.CLASS_LOCATION+0;
            }
            passengers.add(new TaxiPassenger(TaxiConstants.CLASS_PASSENGER + i, startX, startY, goalLocationName));
		}

		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 0, 0, 0, 20, false)); //vertical left
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 1, 0, 0, 20, true)); //horizontal bottom
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 2, 20, 0, 20, false)); //vertical right
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 3, 0, 20, 20, true)); //horizontal top
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 4, 5, 0, 10, false));
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 5, 15, 0, 10, false));
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 6, 10, 10, 10, false));
		//additions to base map
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 7, 8, 6, 5, true ));
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 8, 16, 15, 5, true));
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 9, 0, 15, 5, true));
		walls.add(new TaxiWall(TaxiConstants.CLASS_WALL + 10, 8, 15, 5, true));

		/*
		walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, false));
		walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 5, true));
		walls.add(new TaxiWall(CLASS_WALL + 2, 5, 0, 5, false));
		walls.add(new TaxiWall(CLASS_WALL + 3, 0, 5, 5, true));
		walls.add(new TaxiWall(CLASS_WALL + 4, 1, 0, 2, false));
		walls.add(new TaxiWall(CLASS_WALL + 5, 3, 0, 2, false));
		walls.add(new TaxiWall(CLASS_WALL + 6, 2, 3, 2, false));
		*/
		
		return new TaxiState(taxi, passengers, locations, walls);
	}

	public static TaxiState createClassicStateHalfpoint(boolean inTaxi) {
		TaxiState classic = createClassicState();
		TaxiPassenger passenger = (TaxiPassenger) classic.objectsOfClass(CLASS_PASSENGER).get(0);
		TaxiAgent agent = (TaxiAgent) classic.objectsOfClass(CLASS_TAXI).get(0);
		agent.set(ATT_X, passenger.get(ATT_X));
		agent.set(ATT_Y, passenger.get(ATT_Y));
//		agent.set(ATT_TAXI_OCCUPIED, inTaxi);
		passenger.set(ATT_IN_TAXI, inTaxi);
//		passenger.set(ATT_JUST_PICKED_UP, true);
		return classic;
	}

	public static TaxiState createMediumState() { return createMediumState(1); }

	public static TaxiState createMediumState(int numPassengers) {
		TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 1, 1);

		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
		locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 3, COLOR_YELLOW));
		locations.add(new TaxiLocation(CLASS_LOCATION + 2, 3, 0, COLOR_BLUE));
		locations.add(new TaxiLocation(CLASS_LOCATION + 3, 3, 3, COLOR_GREEN));

		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		for (int i = 0; i < numPassengers; i++){
			// get a random goal
			int startX = 0;
			int startY = 0;
			String goalLocationName = locations.get(RandomFactory.getMapped(0).nextInt(locations.size())).getName();
			String startLocationName = goalLocationName;
			while (startLocationName.equals(goalLocationName)) {
				// put the agent in a random depot that is NOT the goal
				TaxiLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
				startX = (int) startLocation.get(ATT_X);
				startY = (int) startLocation.get(ATT_Y);
				startLocationName = startLocation.getName();
			}
			passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
		}

		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 4, false));
		walls.add(new TaxiWall(CLASS_WALL + 1, 4, 0, 4, false));
		walls.add(new TaxiWall(CLASS_WALL + 2, 0, 0, 4, true));
		walls.add(new TaxiWall(CLASS_WALL + 3, 0, 4, 4, true));

		return new TaxiState(taxi, passengers, locations, walls);
	}

	public static TaxiState createThreeDepots() { return createThreeDepots(1); }

	public static TaxiState createThreeDepots(int numPassengers) {
		TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 3);

		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
		locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 1, COLOR_YELLOW));
		locations.add(new TaxiLocation(CLASS_LOCATION + 2, 0, 2, COLOR_BLUE));

		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		for (int i = 0; i < numPassengers; i++){
			// get a random goal
			int startX = 0;
			int startY = 0;
			String goalLocationName = locations.get(RandomFactory.getMapped(0).nextInt(locations.size())).getName();
			String startLocationName = goalLocationName;
			while (startLocationName.equals(goalLocationName)) {
				// put the agent in a random depot that is NOT the goal
				TaxiLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
				startX = (int) startLocation.get(ATT_X);
				startY = (int) startLocation.get(ATT_Y);
				startLocationName = startLocation.getName();
			}
			passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
		}

		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 4, false));
		walls.add(new TaxiWall(CLASS_WALL + 1, 1, 0, 4, false));
		walls.add(new TaxiWall(CLASS_WALL + 2, 0, 0, 1, true));
		walls.add(new TaxiWall(CLASS_WALL + 3, 0, 4, 1, true));

		return new TaxiState(taxi, passengers, locations, walls);
	}

	public static TaxiState createSmallState() {
	    return createSmallState(1);
    }
	
	public static TaxiState createSmallState(int numPassengers){
		TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 4);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 2, COLOR_RED));
		locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 0, COLOR_YELLOW));
		locations.add(new TaxiLocation(CLASS_LOCATION + 2, 0, 1, COLOR_BLUE));
		locations.add(new TaxiLocation(CLASS_LOCATION + 3, 0, 3, COLOR_GREEN));
	
        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            // normal taxi has original passenger at BLUE depot going to RED depot
            int startX = 0;
            int startY = 1;
            String goalLocationName = CLASS_LOCATION+0;
            // other passengers both start and go to a random depot
            if (i > 0) {
                // get a random goal
                goalLocationName = locations.get(RandomFactory.getMapped(0).nextInt(locations.size())).getName();
                String startLocationName = goalLocationName;
                while (startLocationName.equals(goalLocationName)) {
                    // put the agent in a random depot that is NOT the goal
                    TaxiLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
                    startX = (int) startLocation.get(ATT_X);
                    startY = (int) startLocation.get(ATT_Y);
                    startLocationName = startLocation.getName();
                }
            }
            passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
        }

		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, false));
		walls.add(new TaxiWall(CLASS_WALL + 1, 1, 0, 5, false));
		walls.add(new TaxiWall(CLASS_WALL + 2, 0, 0, 1, true));
		walls.add(new TaxiWall(CLASS_WALL + 3, 0, 5, 1, true));
			
		return new TaxiState(taxi, passengers, locations, walls);
	}

	public static TaxiState createTinyState() { return createTinyState(1); }
	public static TaxiState createTinyState(int numPassengers){
		TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 1);

		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
		locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 1, COLOR_BLUE));

		List<TaxiPassenger> passenger = new ArrayList<TaxiPassenger>();
		for (int i = 0; i < numPassengers; i++) {
			passenger.add(new TaxiPassenger(CLASS_PASSENGER + i, 0, 0, CLASS_LOCATION + 1));
		}

		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 1, true));
		walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 2, false));
		walls.add(new TaxiWall(CLASS_WALL + 2, 0, 2, 1, true));
		walls.add(new TaxiWall(CLASS_WALL + 3, 1, 0, 2, false));

		return new TaxiState(taxi, passenger, locations, walls);
	}
	public static TaxiState createTiny3State(int numPassengers){
		TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 1);

		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
		locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 1, COLOR_BLUE));
		locations.add(new TaxiLocation(CLASS_LOCATION + 2, 0, 2, COLOR_GREEN));

		List<TaxiPassenger> passenger = new ArrayList<TaxiPassenger>();
		for (int i = 0; i < numPassengers; i++) {
			passenger.add(new TaxiPassenger(CLASS_PASSENGER + i, 0, 0, CLASS_LOCATION + 1));
		}

		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 1, true));
		walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 3, false));
		walls.add(new TaxiWall(CLASS_WALL + 2, 0, 3, 1, true));
		walls.add(new TaxiWall(CLASS_WALL + 3, 1, 0, 3, false));

		return new TaxiState(taxi, passenger, locations, walls);
	}
}
