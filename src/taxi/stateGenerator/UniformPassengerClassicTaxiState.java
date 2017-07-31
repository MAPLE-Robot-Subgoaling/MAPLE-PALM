package taxi.stateGenerator;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.*;

import java.util.ArrayList;
import java.util.List;

public class UniformPassengerClassicTaxiState implements StateGenerator{
	private int passengerStart;
	private int passengerGoal;
	private boolean randomTaxiStart;
	public UniformPassengerClassicTaxiState(boolean randomTaxiStart){
		this.passengerGoal=0;
		this.passengerStart=2;
		this.randomTaxiStart = randomTaxiStart;
	}
	public UniformPassengerClassicTaxiState(){
		this(false);
	}
	public int getPassengerStartLocation(){
		return passengerStart;
	}
	public int getPassengerStartGoal(){
		return passengerGoal;
	}
	@Override
	public State generateState() {
		int width = 5;
		int height = 5;
		int tx, ty;
		if(randomTaxiStart) {
			tx = (int) (Math.random() * width);
			ty = (int) (Math.random() * height);
		}
		else{
			tx = 2;
			ty = 2;
		}
		TaxiAgent taxi = new TaxiAgent(Taxi.CLASS_TAXI + 0, tx, ty);
		
		List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 0, 0, 4, Taxi.COLOR_RED));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 1, 0, 0, Taxi.COLOR_YELLOW));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 2, 3, 0, Taxi.COLOR_BLUE));
		locations.add(new TaxiLocation(Taxi.CLASS_LOCATION + 3, 4, 4, Taxi.COLOR_GREEN));
		
		List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
		int px = (int)(locations.get(this.passengerStart).get(Taxi.ATT_X));
		int py = (int)(locations.get(this.passengerStart).get(Taxi.ATT_Y));
		String goalName =  locations.get(this.passengerGoal).name();
		
		passengers.add(new TaxiPassenger(Taxi.CLASS_PASSENGER + 0, px, py, goalName));
		
		List<TaxiWall> walls = new ArrayList<TaxiWall>();
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 0, 0, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 1, 0, 0, 5, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 2, 5, 0, 5, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 3, 0, 5, 5, true));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 4, 1, 0, 2, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 5, 3, 0, 2, false));
		walls.add(new TaxiWall(Taxi.CLASS_WALL + 6, 2, 3, 2, false));
		if(this.passengerStart==1)
			this.passengerGoal= (this.passengerGoal+1)%4;
		this.passengerStart = (this.passengerStart+1)%4;
		return new TaxiState(taxi, passengers, locations, walls);
	}

}
