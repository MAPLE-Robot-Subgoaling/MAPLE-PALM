package taxi.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;
import static taxi.TaxiConstants.*;

public class TaxiWall extends MutableObject {

	/**
	 * contains startx and y and length and if it is horizontal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_START_X,
			ATT_START_Y,
			ATT_LENGTH,
			ATT_IS_HORIZONTAL
			);
	
	public TaxiWall(String name, int startX, int startY, int length, boolean isHorizontal) {
		this(name, (Object) startX, (Object) startY, (Object) length, (Object) isHorizontal);
	}
	
	public TaxiWall(String name, Object startX, Object startY, Object length, Object isHorizontal) {
		this.set(ATT_START_X, startX);
		this.set(ATT_START_Y, startY);
		this.set(ATT_LENGTH, length);
		this.set(ATT_IS_HORIZONTAL, isHorizontal);
		this.setName(name);
	}
		
	@Override
	public String className() {
		return CLASS_WALL;
	}

	@Override
	public TaxiWall copy() {
		return (TaxiWall) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiWall(
				objectName,
				get(ATT_START_X),
				get(ATT_START_Y),
				get(ATT_LENGTH),
				get(ATT_IS_HORIZONTAL)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
