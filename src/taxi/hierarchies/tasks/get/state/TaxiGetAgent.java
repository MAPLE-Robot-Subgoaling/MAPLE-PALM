package taxi.hierarchies.tasks.get.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiGetAgent extends MutableObject {

	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiGetDomain.ATT_LOCATION
			);

	public TaxiGetAgent(String name, String location) {
		this(name, (Object)location);
	}

	private TaxiGetAgent(String name, Object location) {
	    this.set(TaxiGetDomain.ATT_LOCATION, location);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiNavState.CLASS_TAXI;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiGetAgent( objectName, get(TaxiGetDomain.ATT_LOCATION));
	}

	@Override
	public TaxiGetAgent copy() {
		return (TaxiGetAgent) copyWithName(name());
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
