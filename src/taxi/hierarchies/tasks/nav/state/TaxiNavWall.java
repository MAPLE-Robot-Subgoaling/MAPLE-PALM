package taxi.hierarchies.tasks.nav.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

public class TaxiNavWall extends MutableObject {

	/**
	 * contains startx and y and length and if it is horizontal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_START_X,
			Taxi.ATT_START_Y,
			Taxi.ATT_LENGTH,
			Taxi.ATT_IS_HORIZONTAL
			);
	
	public TaxiNavWall(String name, int startX, int startY, int length, boolean isHorizontal) {
		this(name, (Object) startX, (Object) startY, (Object) length, (Object) isHorizontal);
	}
	
	public TaxiNavWall(String name, Object startX, Object startY, Object length, Object isHorizontal) {
		this.set(Taxi.ATT_START_X, startX);
		this.set(Taxi.ATT_START_Y, startY);
		this.set(Taxi.ATT_LENGTH, length);
		this.set(Taxi.ATT_IS_HORIZONTAL, isHorizontal);
		this.setName(name);
	}
		
	@Override
	public String className() {
		return TaxiNavState.CLASS_WALL;
	}

	@Override
	public TaxiNavWall copy() {
		return (TaxiNavWall) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiNavWall(
				objectName,
				get(Taxi.ATT_START_X),
				get(Taxi.ATT_START_Y),
				get(Taxi.ATT_LENGTH),
				get(Taxi.ATT_IS_HORIZONTAL)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
