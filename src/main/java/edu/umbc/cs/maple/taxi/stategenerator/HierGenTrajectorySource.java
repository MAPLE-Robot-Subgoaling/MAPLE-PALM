package edu.umbc.cs.maple.taxi.stategenerator;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.config.taxi.TaxiConfig;
import edu.umbc.cs.maple.taxi.state.*;
import edu.umbc.cs.maple.utilities.BurlapConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
import static edu.umbc.cs.maple.taxi.TaxiConstants.CLASS_WALL;

public class HierGenTrajectorySource implements StateGenerator {

    public static final int HIERGEN_STATE_GENERATOR_RNG_INDEX = 177119;
    private String stateType;
    private boolean useDifferentSeedsEachTime;

    public HierGenTrajectorySource(long seed, String stateType, boolean useDifferentSeedsEachTime) {
        this.stateType = stateType;
        this.useDifferentSeedsEachTime = useDifferentSeedsEachTime;
        Random rng = RandomFactory.getMapped(HIERGEN_STATE_GENERATOR_RNG_INDEX);
        rng.setSeed(seed);
    }

    @Override
    public State generateState() {
        TaxiConfig config = new TaxiConfig();
        config.state = stateType;
        if (useDifferentSeedsEachTime) {
            Random rng = RandomFactory.getMapped(HIERGEN_STATE_GENERATOR_RNG_INDEX);
            long seed = rng.nextLong();
            System.out.println("Generating state using seed: " + seed);
            rng.setSeed(seed);
        }
        return config.generateState();
    }


//    @Override
//    public State generateState() {
//
//        int width = 2;
//        int height = 2;
//
//        int tx = 0;//RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX).nextInt(width);
//        int ty = 1;//RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX).nextInt(height);
//
//        TaxiAgent taxi = new TaxiAgent(CLASS_TAXI + 0, tx, ty);
//
//        List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
//        locations.add(new TaxiLocation(CLASS_LOCATION + 0, 0, 1, COLOR_RED));
//        locations.add(new TaxiLocation(CLASS_LOCATION + 1, 0, 0, COLOR_YELLOW));
//        locations.add(new TaxiLocation(CLASS_LOCATION + 2, 1, 0, COLOR_BLUE));
//        locations.add(new TaxiLocation(CLASS_LOCATION + 3, 1, 1, COLOR_GREEN));
//
//        List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
//
////        int start = (int)(RandomFactory.getMapped(0).nextDouble() * 4);
////        int px = (int)(locations.get(start).get(ATT_X));
////        int py = (int)(locations.get(start).get(ATT_Y));
////        int goal;
////        do
////            goal = (int)(RandomFactory.getMapped(0).nextDouble() * 4);
////        while(goal==start);
////
////        String goalName =  locations.get(goal).name();
//        int px = 1;
//        int py = 0;
//        String goalName = locations.get(0).name();
//
//        passengers.add(new TaxiPassenger(CLASS_PASSENGER + 0, px, py, goalName));
//
//        List<TaxiWall> walls = new ArrayList<TaxiWall>();
//        walls.add(new TaxiWall(CLASS_WALL + 0, 0, 0, 2, false));
//        walls.add(new TaxiWall(CLASS_WALL + 1, 0, 0, 2, true));
//        walls.add(new TaxiWall(CLASS_WALL + 2, 2, 0, 2, false));
//        walls.add(new TaxiWall(CLASS_WALL + 3, 0, 2, 2, true));
//
//        return new TaxiState(taxi, passengers, locations, walls);
//    }
}
