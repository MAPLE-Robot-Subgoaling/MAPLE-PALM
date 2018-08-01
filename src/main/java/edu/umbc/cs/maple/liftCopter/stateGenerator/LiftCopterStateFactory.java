package edu.umbc.cs.maple.liftCopter.stateGenerator;

import burlap.debugtools.RandomFactory;
import edu.umbc.cs.maple.liftCopter.state.*;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LiftCopterStateFactory {
    public static LiftCopterState createClassicState() {
        return createClassicState(1);
    }

    public static LiftCopterState createClassicState(int numCargos){
        LiftCopterAgent agent = new LiftCopterAgent(CLASS_AGENT + 0, 0.25,3.25, .5, .5);

        List<LiftCopterLocation> locations = new ArrayList<LiftCopterLocation>();
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 0, 0.0, 4.0,1,1, COLOR_RED));
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 1, 2.0, 0.0,1,1, COLOR_YELLOW));
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 2, 4.0, 0.0,1,1, COLOR_BLUE));
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 3, 4.0, 4.0,1,1, COLOR_GREEN));

        List<LiftCopterCargo> cargos = new ArrayList<LiftCopterCargo>();
        for (int i = 0; i < numCargos; i++){
            // classic agent has original cargo at BLUE depot going to RED depot
            double startX = 0.0;
            double startY = 4.0;
            String goalLocationName = CLASS_LOCATION+1;
            // other cargos both start and go to a random depot
            if (i > 0) {
//                 get a random goal
                goalLocationName = locations.get(RandomFactory.getMapped(0).nextInt(locations.size())).getName();
                String startLocationName = goalLocationName;
                while (startLocationName.equals(goalLocationName)) {
                    // put the agent in a random depot that is NOT the goal
                    LiftCopterLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
                    startX =  (double) startLocation.get(ATT_X);
                    startY =  (double) startLocation.get(ATT_Y);
                    startLocationName = startLocation.getName();
                }
//                startX = 3;
//                startY = 0;
//                goalLocationName = CLASS_LOCATION+0;
            }
            cargos.add(new LiftCopterCargo(CLASS_CARGO + i, startX, startY,1,1, goalLocationName));
        }

        List<LiftCopterWall> walls = new ArrayList<LiftCopterWall>();
        walls.add(new LiftCopterWall(CLASS_WALL + 0, 0, 0, 0.1, 5));
        walls.add(new LiftCopterWall(CLASS_WALL + 1, 0, 0, 5, 0.1));
        walls.add(new LiftCopterWall(CLASS_WALL + 2, 4.9, 0, 0.1, 5));
        walls.add(new LiftCopterWall(CLASS_WALL + 3, 0, 4.9, 5, 0.1));
        walls.add(new LiftCopterWall(CLASS_WALL + 4, 1, 0, 0.1, 2));
        walls.add(new LiftCopterWall(CLASS_WALL + 5, 3, 0, 0.1, 2));
        walls.add(new LiftCopterWall(CLASS_WALL + 6, 2, 3, 0.1, 2));

        return new LiftCopterState(agent, cargos, locations, walls);
    }
    
    public static LiftCopterState createClassicStateHalfpoint(boolean pickedUp) {
        LiftCopterState classic = createClassicState();
        LiftCopterCargo cargo = (LiftCopterCargo) classic.objectsOfClass(CLASS_CARGO).get(0);
        LiftCopterAgent agent = (LiftCopterAgent) classic.objectsOfClass(CLASS_AGENT).get(0);
        agent.set(ATT_X, cargo.get(ATT_X));
        agent.set(ATT_Y, cargo.get(ATT_Y));
//		agent.set(ATT_AGENT_OCCUPIED, inAgent);
        cargo.set(ATT_PICKED_UP, pickedUp);
//		cargo.set(ATT_JUST_PICKED_UP, true);
        return classic;
    }
    public static LiftCopterState createMiniState(){
        LiftCopterAgent agent = new LiftCopterAgent(CLASS_AGENT + 0, 0.25,1.25, .5, .5);

        List<LiftCopterLocation> locations = new ArrayList<LiftCopterLocation>();
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 0, 0, 1,1,1, COLOR_GREEN));
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 1, 0, 0,1,1, COLOR_YELLOW));

        List<LiftCopterCargo> cargos = new ArrayList<LiftCopterCargo>();
        for (int i = 0; i < 1; i++){
            // classic agent has original cargo at BLUE depot going to RED depot
            double startX = 0;
            double startY = 1.1;
            String goalLocationName = CLASS_LOCATION+1;
            LiftCopterCargo beej = new LiftCopterCargo(CLASS_CARGO + i, startX, startY,1,1, goalLocationName);
                    beej.set(ATT_PICKED_UP, true);
            cargos.add(beej);

        }

        List<LiftCopterWall> walls = new ArrayList<LiftCopterWall>();
        walls.add(new LiftCopterWall(CLASS_WALL + 0, 0, 0, 0.1, 2));
        walls.add(new LiftCopterWall(CLASS_WALL + 1, 0, 0, 1, 0.1));
        walls.add(new LiftCopterWall(CLASS_WALL + 2, .9, 0, 0.1, 2));
        walls.add(new LiftCopterWall(CLASS_WALL + 3, 0, 1.9, 1, 0.1));

        return new LiftCopterState(agent, cargos, locations, walls);
    }
}
