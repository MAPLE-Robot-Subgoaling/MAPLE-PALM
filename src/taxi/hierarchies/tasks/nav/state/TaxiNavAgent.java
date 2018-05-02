package taxi.hierarchies.tasks.nav.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import utilities.MutableObject;
import static taxi.TaxiConstants.*;

public class TaxiNavAgent extends MutableObject {

	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_X,
			ATT_Y
			);
	
	public TaxiNavAgent(String name, int x, int y) {
		this(name, (Object) x, (Object) y);
	}
	
	private TaxiNavAgent(String name, Object x, Object y) {
		this.set(ATT_X, x);
		this.set(ATT_Y, y);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return CLASS_TAXI;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiNavAgent(
				objectName, 
				get(ATT_X),
				get(ATT_Y));
	}

	@Override
	public TaxiNavAgent copy() {
		return (TaxiNavAgent) copyWithName(name());
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
