package edu.umbc.cs.maple.taxi.stategenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import burlap.debugtools.RandomFactory;
import edu.umbc.cs.maple.taxi.TaxiConstants;
import edu.umbc.cs.maple.taxi.state.TaxiAgent;
import edu.umbc.cs.maple.taxi.state.TaxiLocation;
import edu.umbc.cs.maple.taxi.state.TaxiPassenger;
import edu.umbc.cs.maple.taxi.state.TaxiState;
import edu.umbc.cs.maple.taxi.state.TaxiWall;
import edu.umbc.cs.maple.utilities.Utils;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;


public class TaxiStateFactory {

    //generates taxi states

    private static TaxiState createRandomState(int numPassengers, int numDepots) {

        if (numDepots < 2 || numPassengers < 1) {
            throw new RuntimeException("Error: must have 1+ passengers and 2+ depots");
        }

        Random rng = RandomFactory.getMapped(0);

        int taxiX = rng.nextInt(5);
        int taxiY = rng.nextInt(5);
        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, taxiX, taxiY);

        List<TaxiLocation> depots = new ArrayList<>();
        for (int i = 0; i < numDepots; i++) {
            int depotX = rng.nextInt(5);
            int depotY = rng.nextInt(5);
            String depotColor = TaxiConstants.COLORS[rng.nextInt(TaxiConstants.COLORS.length)];
            TaxiLocation depot = new TaxiLocation(CLASS_LOCATION+i, depotX, depotY, depotColor);
            depots.add(depot);
        }

        List<TaxiPassenger> passengers = new ArrayList<>();
        for (int i = 0; i < numPassengers; i++) {
            TaxiLocation start = Utils.choice(depots, rng);
            TaxiLocation goal;
            do {
                goal = Utils.choice(depots, rng);
            } while(start == goal);
            int passengerX = (int) start.get(ATT_X);
            int passengerY = (int) start.get(ATT_Y);
            String passengerGoal = goal.name();
            TaxiPassenger passenger = new TaxiPassenger(CLASS_PASSENGER+i, passengerX, passengerY, passengerGoal);
            passengers.add(passenger);
        }

        List<TaxiWall> walls = new ArrayList<TaxiWall>();
        // a box always exists around the domain
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 5, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 0, 5, 5, true));
        int nextWallIndex = 4;

        // now flip a coin for either vertical or horizontal mini-walls
        boolean isHorizontal = rng.nextBoolean();
        int numMiniWallsOnLeft = rng.nextInt(5);
        int numMiniWallsOnRight = rng.nextInt(5);
        for (int i = 0; i < numMiniWallsOnLeft + numMiniWallsOnRight; i++) {
            int position = i;
            int length = rng.nextInt(2) + 1;
            int anchor = position < numMiniWallsOnLeft ? 0 : 5 - length;
            position = position < numMiniWallsOnLeft ? position : position - numMiniWallsOnLeft;
            int startX = isHorizontal ? anchor : position + 1;
            int startY = isHorizontal ? position + 1 : anchor;
            TaxiWall wall = new TaxiWall(CLASS_WALL + nextWallIndex, startX, startY, length, isHorizontal);
            nextWallIndex += 1;
            walls.add(wall);
        }

        TaxiState state = new TaxiState(taxi, passengers, depots, walls);
        return state;
    }

    public static TaxiState createClassicState() {
        return createClassicState(1);
    }

    public static TaxiState createClassicState(int numPassengers){
        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 3);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 4, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 0, COLOR_YELLOW));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 3, 0, COLOR_BLUE));
        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 4, 4, COLOR_GREEN));

        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            // classic taxi has original passenger at BLUE depot going to RED depot
            int startX = 3;
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
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 5, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 0, 5, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 4, 1, 0, 2, false));
        walls.add(new TaxiWall(CLASS_WALL + 5, 3, 0, 2, false));
        walls.add(new TaxiWall(CLASS_WALL + 6, 2, 3, 2, false));

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


    public static TaxiState createClassic20State(int numPassengers){
        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 12);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 19, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 0, COLOR_YELLOW));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 12, 0, COLOR_BLUE));
        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 19, 19, COLOR_GREEN));

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
//                goalLocationName = Taxi.CLASS_LOCATION+0;
            }
            passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
        }

        List<TaxiWall> walls = new ArrayList<TaxiWall>();
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 20, false)); //vertical left
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 20, true)); //horizontal bottom
        walls.add(new TaxiWall(CLASS_WALL + 2, 20, 0, 20, false)); //vertical right
        walls.add(new TaxiWall(CLASS_WALL + 3, 0, 20, 20, true)); //horizontal top
        walls.add(new TaxiWall(CLASS_WALL + 4, 5, 0, 10, false));
        walls.add(new TaxiWall(CLASS_WALL + 5, 15, 0, 10, false));
        walls.add(new TaxiWall(CLASS_WALL + 6, 10, 10, 10, false));
        //additions to base map
        walls.add(new TaxiWall(CLASS_WALL + 7, 8, 6, 5, true ));
        walls.add(new TaxiWall(CLASS_WALL + 8, 16, 15, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 9, 0, 15, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 10, 8, 15, 5, true));

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

    public static TaxiState createStepTest(int numPassengers) {
        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 0);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 1, 0, COLOR_YELLOW));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 3, 0, COLOR_BLUE));
        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 2, 0, COLOR_GREEN));

        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            int startX = 2;
            int startY = 0;
            String goalLocationName = CLASS_LOCATION+0;
            // other passengers both start and go to a random depot
            if (i > 0) {
                startX = 3;
            }
            passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
        }

        List<TaxiWall> walls = new ArrayList<TaxiWall>();
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 4, true));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 1, 4, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 0, 0, 1, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 4, 0, 1, false));

        return new TaxiState(taxi, passengers, locations, walls);
    }
    public static TaxiState createDiscountTest(int numPassengers) {

        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 0);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 20, 0, COLOR_BLUE));

        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            int startX = 20;
            int startY = 0;
            String goalLocationName = CLASS_LOCATION+0;
            passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
        }

        List<TaxiWall> walls = new ArrayList<TaxiWall>();
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 21, true));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 1, 21, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 0, 0, 1, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 21, 0, 1, false));

        return new TaxiState(taxi, passengers, locations, walls);
    }

    public static TaxiState createDiscountTestBig(int numPassengers) {

        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 0);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 1, 4, COLOR_GREEN));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 2, 4, COLOR_YELLOW));
        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 3, 0, COLOR_BLUE));

        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            int startX = 3;
            int startY = 0;
            String goalLocationName = CLASS_LOCATION+0;
            passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
        }

        List<TaxiWall> walls = new ArrayList<TaxiWall>();
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 5, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 0, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 5, 0, 5, false));

        return new TaxiState(taxi, passengers, locations, walls);
    }
    public static TaxiState createDiscountTestSmall(int numPassengers) {

        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 0, 0);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 0, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 1, COLOR_GREEN));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 4, 1, COLOR_YELLOW));
        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 3, 0, COLOR_BLUE));

        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            int startX = 3;
            int startY = 0;
            String goalLocationName = CLASS_LOCATION+0;
            passengers.add(new TaxiPassenger(CLASS_PASSENGER + i, startX, startY, goalLocationName));
        }

        List<TaxiWall> walls = new ArrayList<TaxiWall>();
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 2, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 0, 0, 2, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 5, 0, 2, false));

        return new TaxiState(taxi, passengers, locations, walls);
    }
    public static TaxiState createMehtaZigZag1State(int numPassengers){
        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 2, 2);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 4, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 0, COLOR_YELLOW));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 3, 0, COLOR_BLUE));
        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 4, 4, COLOR_GREEN));

        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            // classic taxi has original passenger at BLUE depot going to RED depot
            int startX = 3;
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
        //add boundary walls
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 5, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 0, 5, 5, true));
        //add internal walls
        walls.add(new TaxiWall(CLASS_WALL + 4, 1, 4, 4, true));
        walls.add(new TaxiWall(CLASS_WALL + 5, 0, 3, 4, true));
        walls.add(new TaxiWall(CLASS_WALL + 6, 1, 2, 4, true));
        walls.add(new TaxiWall(CLASS_WALL + 7, 0, 1, 4, true));

        return new TaxiState(taxi, passengers, locations, walls);
    }

    public static TaxiState createMehtaZigZag2State(int numPassengers){
        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, 2, 2);

        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 4, COLOR_RED));
        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 0, COLOR_YELLOW));
        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 3, 0, COLOR_BLUE));
        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 4, 4, COLOR_GREEN));

        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
        for (int i = 0; i < numPassengers; i++){
            // classic taxi has original passenger at BLUE depot going to RED depot
            int startX = 3;
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
        //add boundary walls
        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 5, true));
        walls.add(new TaxiWall(CLASS_WALL + 2, 5, 0, 5, false));
        walls.add(new TaxiWall(CLASS_WALL + 3, 0, 5, 5, true));
        //add internal walls
        walls.add(new TaxiWall(CLASS_WALL + 4, 1, 1, 4, false));
        walls.add(new TaxiWall(CLASS_WALL + 5, 2, 0, 4, false));
        walls.add(new TaxiWall(CLASS_WALL + 6, 3, 1, 4, false));
        walls.add(new TaxiWall(CLASS_WALL + 7, 4, 0, 4, false));

        return new TaxiState(taxi, passengers, locations, walls);
    }

    public static TaxiState generateState(String state) {

        String passengerNumberRegex = "\\-(\\d+)passengers";
        Pattern r = Pattern.compile(passengerNumberRegex);
        Matcher m = r.matcher(state);
        String numPassengersString = "";
        if (m.find()) {
            numPassengersString = m.group(1);
        }

        String passengerDepotRegex = "\\-(\\d+)d\\-(\\d+)p";
        Pattern r2 = Pattern.compile(passengerDepotRegex);
        Matcher m2 = r2.matcher(state);
        String numDepotsString = "";
        if (m2.find()) {
            numDepotsString = m2.group(1);
            numPassengersString = m2.group(2);
        }

        int numPassengers = "".equals(numPassengersString) ? 1 : Integer.parseInt(numPassengersString);
        int numDepots = "".equals(numDepotsString) ? 1 : Integer.parseInt(numDepotsString);

        if        (state.equals("classic")) {
            return TaxiStateFactory.createClassicState();
        } else if (state.equals("tiny")) {
            return TaxiStateFactory.createTinyState();
        } else if (state.equals("small")) {
            return TaxiStateFactory.createSmallState();
        } else if (state.equals("medium")) {
            return TaxiStateFactory.createMediumState();
        } else if (state.equals("3depots")) {
            return TaxiStateFactory.createThreeDepots();
        } else if (state.equals("mehta-zigzag-1")) {
            return TaxiStateFactory.createMehtaZigZag1State(1);
        } else if (state.equals("mehta-zigzag-2")) {
            return TaxiStateFactory.createMehtaZigZag2State(1);
        } else if (state.equals("steptest")) {
            return TaxiStateFactory.createStepTest(1);
        } else if (state.matches("classic" + passengerNumberRegex)) {
            return TaxiStateFactory.createClassicState(numPassengers);
        } else if (state.matches("classic20" + passengerNumberRegex)) {
            return TaxiStateFactory.createClassic20State(numPassengers);
        } else if (state.matches("tiny" + passengerNumberRegex)) {
            return TaxiStateFactory.createTinyState(numPassengers);
        } else if (state.matches("tiny3" + passengerNumberRegex)) {
            return TaxiStateFactory.createTiny3State(numPassengers);
        } else if (state.matches("small" + passengerNumberRegex)) {
            return TaxiStateFactory.createSmallState(numPassengers);
        } else if (state.matches("medium" + passengerNumberRegex)) {
            return TaxiStateFactory.createMediumState(numPassengers);
        } else if (state.matches("3depots" + passengerNumberRegex)) {
            return TaxiStateFactory.createThreeDepots(numPassengers);
        } else if (state.matches("mehta-zigzag-1" + passengerNumberRegex)) {
            return TaxiStateFactory.createMehtaZigZag1State(numPassengers);
        } else if (state.matches("mehta-zigzag-2" + passengerNumberRegex)) {
            return TaxiStateFactory.createMehtaZigZag2State(numPassengers);
        } else if (state.matches("steptest" + passengerNumberRegex)) {
            return TaxiStateFactory.createStepTest(numPassengers);
        } else if (state.matches("discounttest" + passengerNumberRegex)) {
            return TaxiStateFactory.createDiscountTest(numPassengers);
        } else if (state.matches("discounttestbig" + passengerNumberRegex)) {
            return TaxiStateFactory.createDiscountTestBig(numPassengers);
        } else if (state.matches("discounttestsmall" + passengerNumberRegex)) {
            return TaxiStateFactory.createDiscountTestSmall(numPassengers);
        }  else if (state.matches("random" + passengerDepotRegex)) {
            return TaxiStateFactory.createRandomState(numPassengers, numDepots);
        } else {
            throw new RuntimeException("ERROR: invalid state passed to generateState in TaxiConfig: " + state);
        }
    }

}