package taxi.hierarchies.tasks.nav.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import utilities.MutableObject;

public class TaxiNavWall extends MutableObject {

	/**
	 * contains startx and y and length and if it is horizontal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiNavDomain.ATT_START_X,
			TaxiNavDomain.ATT_START_Y,
			TaxiNavDomain.ATT_LENGTH,
			TaxiNavDomain.ATT_IS_HORIZONTAL
			);
	
	public TaxiNavWall(String name, int startX, int startY, int length, boolean isHorizontal) {
		this(name, (Object) startX, (Object) startY, (Object) length, (Object) isHorizontal);
	}
	
	public TaxiNavWall(String name, Object startX, Object startY, Object length, Object isHorizontal) {
		this.set(TaxiNavDomain.ATT_START_X, startX);
		this.set(TaxiNavDomain.ATT_START_Y, startY);
		this.set(TaxiNavDomain.ATT_LENGTH, length);
		this.set(TaxiNavDomain.ATT_IS_HORIZONTAL, isHorizontal);
		this.setName(name);
	}
		
	@Override
	public String className() {
		return Taxi.CLASS_WALL;
	}

	@Override
	public TaxiNavWall copy() {
		return (TaxiNavWall) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiNavWall(
				objectName,
				get(TaxiNavDomain.ATT_START_X),
				get(TaxiNavDomain.ATT_START_Y),
				get(TaxiNavDomain.ATT_LENGTH),
				get(TaxiNavDomain.ATT_IS_HORIZONTAL)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
