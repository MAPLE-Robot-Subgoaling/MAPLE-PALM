
package taxi.stateGenerator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
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
	public static TaxiState createTallState(){
        return createVariantTallState("A", "red");
	}

	public static TaxiState createVariantTallState(String startDepot, String targetColor){
        //init agent
        TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI+0, 1, 3);
        //init locations, each with three colors
        List<TaxiLocation> locations = new ArrayList<>();
        List<String> AColors = new ArrayList<>();
        AColors.add(Taxi.COLOR_MAGENTA);
        AColors.add(Taxi.COLOR_RED);
        AColors.add(Taxi.COLOR_BLUE);
        locations.add(new TaxiLocation(Taxi.CLASS_LOCATION+"A", 0,6, AColors));
        List<String> BColors = new ArrayList<>();
        BColors.add(Taxi.COLOR_GREEN);
        BColors.add(Taxi.COLOR_YELLOW);
        BColors.add(Taxi.COLOR_BLUE);
        locations.add(new TaxiLocation(Taxi.CLASS_LOCATION+"B", 0,0, BColors));
        //init passenger, goal is either location A or color red
        int x = -1, y = -1;
        switch(startDepot){
            case "A": x=0; y=0;break;
            case "B": x=0; y=6;break;
        }
        List<TaxiPassenger> passenger =Collections.singletonList(
                new TaxiPassenger(Taxi.CLASS_PASSENGER+0, x,y,
                        targetColor/*Taxi.CLASS_LOCATION+"A"*/));
        //init walls
        List<TaxiWall> walls = new ArrayList<TaxiWall>();
        walls.add(new TaxiWall(Taxi.CLASS_WALL+0, 0, 0, 7, false));
        walls.add(new TaxiWall(Taxi.CLASS_WALL+1, 2, 0, 7, false));
        walls.add(new TaxiWall(Taxi.CLASS_WALL+2, 0,0, 2, true));
        walls.add(new TaxiWall(Taxi.CLASS_WALL+3, 0,7,2, true));

        walls.add(new TaxiWall(Taxi.CLASS_WALL+4, 0, 1, 1, true));
        walls.add(new TaxiWall(Taxi.CLASS_WALL+5, 1, 3, 1, true));
        walls.add(new TaxiWall(Taxi.CLASS_WALL+6, 0, 5, 1, true));
        return new TaxiState(taxi, passenger, locations, walls);
    }
	public static TaxiState createClassicState(){
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, 0, 3);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 4, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 0, Taxi.COLOR_YELLOW));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 2, 3, 0, Taxi.COLOR_BLUE));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 3, 4, 4, Taxi.COLOR_GREEN));
		
		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();

    passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, 3, 0, Taxi.COLOR_RED));

		
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
		passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, 0, 1, Taxi.COLOR_RED));
		
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
		passenger.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, 0, 0, Taxi.COLOR_BLUE));
		
		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 0, 0, 0, 1, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 1, 0, 0, 2, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 2, 0, 2, 1, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 3, 1, 0, 2, false));
		
		return new TaxiState(taxi, passenger, locations, walls);
	}
}
