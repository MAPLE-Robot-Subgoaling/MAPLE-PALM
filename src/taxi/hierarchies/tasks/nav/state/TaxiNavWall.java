package taxi.hierarchies.tasks.nav.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import utilities.MutableObject;
import static taxi.TaxiConstants.*;

public class TaxiNavWall extends MutableObject {

	/**
	 * contains startx and y and length and if it is horizontal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_START_X,
			ATT_START_Y,
			ATT_LENGTH,
			ATT_IS_HORIZONTAL
			);
	
	public TaxiNavWall(String name, int startX, int startY, int length, boolean isHorizontal) {
		this(name, (Object) startX, (Object) startY, (Object) length, (Object) isHorizontal);
	}
	
	public TaxiNavWall(String name, Object startX, Object startY, Object length, Object isHorizontal) {
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
	public TaxiNavWall copy() {
		return (TaxiNavWall) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiNavWall(
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

    public boolean blocksMovement(int tx, int ty, int dx, int dy) {
		int wx = (int)get(ATT_START_X);
		int wy = (int)get(ATT_START_Y);
		int wl = (int)get(ATT_LENGTH);
		boolean isHorizontal = (boolean)get(ATT_IS_HORIZONTAL);
		boolean betweenX = wx <= tx && tx < (wx+wl);
		boolean betweenY = wy <= ty && ty < (wy+wl);
		if (isHorizontal && dy > 0) { // going north
			// if just below wall, block
			boolean justBelow = ty + 1 == wy;
			return justBelow && betweenX;
		}
		if (isHorizontal && dy < 0) { // going south
			// if just above wall, block
			boolean justAbove = ty == wy;
			return justAbove && betweenX;
		}
		if (!isHorizontal && dx > 0) { // going east
			// if just left of wall, block
			boolean justLeft = tx+1 == wx;
			return justLeft && betweenY;
		}
		if (!isHorizontal && dx < 0) { // going west
			// if just right of wall, block
			boolean justRight = tx == wx;
			return justRight && betweenY;
		}
		// if none of the cases applied, it does not block movement
		return false;
	}

}
