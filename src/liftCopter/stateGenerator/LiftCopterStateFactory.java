package liftCopter.stateGenerator;

import burlap.debugtools.RandomFactory;
import liftCopter.LiftCopter;
import liftCopter.state.*;

import java.util.ArrayList;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;

public class LiftCopterStateFactory {
    public static LiftCopterState createClassicState() {
        return createClassicState(1);
    }

    public static LiftCopterState createClassicState(int numPassengers){
        LiftCopterAgent taxi = new LiftCopterAgent(CLASS_AGENT + 0, 0.25,3.25, .5, .5);

        List<LiftCopterLocation> locations = new ArrayList<LiftCopterLocation>();
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 0, .5D, 4.5,1,1, COLOR_RED));
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 1, .5, .5,1,1, COLOR_YELLOW));
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 2, 3.5, .5,1,1, COLOR_BLUE));
        locations.add(new LiftCopterLocation(CLASS_LOCATION + 3, 4.5, 4.5,1,1, COLOR_GREEN));

        List<LiftCopterCargo> cargos = new ArrayList<LiftCopterCargo>();
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
                    LiftCopterLocation startLocation = locations.get(RandomFactory.getMapped(0).nextInt(locations.size()));
                    startX = (int) startLocation.get(ATT_X);
                    startY = (int) startLocation.get(ATT_Y);
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

        return new LiftCopterState(taxi, cargos, locations, walls);
    }
}
