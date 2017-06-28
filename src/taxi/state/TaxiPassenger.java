package taxi.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

public class TaxiPassenger extends MutableObject{

	/**
	 * x, y, whether in taxi, goal, whether they have been picked up, whether they wer just picked up
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y,
			Taxi.ATT_IN_TAXI,
			Taxi.ATT_GOAL_LOCATION,
			Taxi.ATT_PICKED_UP_AT_LEAST_ONCE,
			Taxi.ATT_JUST_PICKED_UP
			);
	
	public TaxiPassenger(String name, int x, int y, String goalColor){
		this(name, (Object) x, (Object) y, (Object) goalColor, false, false, false);
	}
	
	public TaxiPassenger(String name, int x, int y, String goalColor, boolean inTaxi){
		this(name, (Object) x, (Object) y, (Object) goalColor, (Object) inTaxi, false, false);
	}
	
	public TaxiPassenger(String name, int x, int y, String goalColor, boolean inTaxi,
			boolean pickedUpAlLeastOnce, boolean justPickedUp){
		this(name, (Object) x, (Object) y, (Object) goalColor, (Object) inTaxi,
				(Object) pickedUpAlLeastOnce, (Object) justPickedUp);
	}
	
	private TaxiPassenger(String name, Object x, Object y, Object goalColor, Object inTaxi,
			Object pickedUpAtLeastOnce, Object justpickedUp){
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.set(Taxi.ATT_GOAL_LOCATION, goalColor);
		this.set(Taxi.ATT_IN_TAXI, inTaxi);
		this.set(Taxi.ATT_PICKED_UP_AT_LEAST_ONCE, pickedUpAtLeastOnce);
		this.set(Taxi.ATT_JUST_PICKED_UP, justpickedUp);
		this.setName(name);
	}
	

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiPassenger(
				objectName,
				get(Taxi.ATT_X),
				get(Taxi.ATT_Y),
				get(Taxi.ATT_GOAL_LOCATION),
				get(Taxi.ATT_IN_TAXI),
				get(Taxi.ATT_PICKED_UP_AT_LEAST_ONCE),
				get(Taxi.ATT_JUST_PICKED_UP)
				);
	}
	
	@Override
	public TaxiPassenger copy() {
		return (TaxiPassenger) copyWithName(name());
	}
	
	@Override
	public String className() {
		return Taxi.CLASS_PASSENGER;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
