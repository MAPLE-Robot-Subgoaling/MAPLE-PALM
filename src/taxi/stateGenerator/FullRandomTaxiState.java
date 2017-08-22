package taxi.stateGenerator;

import java.awt.Point;
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

public class FullRandomTaxiState implements StateGenerator {

	@Override
	public State generateState(){ 
		int width = 5;
		int hieght = 5;
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		String[] colors = {Taxi.COLOR_RED, Taxi.COLOR_YELLOW, Taxi.COLOR_BLUE, Taxi.COLOR_GREEN};
		List<Point> points = new ArrayList<Point>();
		
		while(locations.size() < 4){
			int lx = (int) (RandomFactory.getMapped(0).nextDouble() * width);
			int ly = (int) (RandomFactory.getMapped(0).nextDouble() * hieght);
			Point newp = new Point(lx, ly);
			if(!points.contains(newp)){
				points.add(newp);
				TaxiLocation loc = new TaxiLocation(Taxi.CLASS_LOCATION + locations.size(), lx, ly, 
						colors[locations.size()]);
				locations.add(loc);
			}
		}
		
		int passengerLoc = (int) (RandomFactory.getMapped(0).nextDouble() * 4);
		Point loc = points.get(passengerLoc);
		int goal = (int) (RandomFactory.getMapped(0).nextDouble() * 4);
		String goalName = locations.get(goal).name();
		TaxiPassenger p = new TaxiPassenger(Taxi.CLASS_PASSENGER, loc.x, loc.y, goalName);
		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		passengers.add(p);
		
		int tx = (int)(RandomFactory.getMapped(0).nextDouble() * width);
		int ty = (int)(RandomFactory.getMapped(0).nextDouble() * hieght);
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, tx, ty);
		
		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 0, 0, 0, width, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 1, 0, 0, hieght, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 2, 0, hieght, width, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 3, width, 0, hieght, false));
		
		return new TaxiState(taxi, passengers, locations, walls);
	}

}
