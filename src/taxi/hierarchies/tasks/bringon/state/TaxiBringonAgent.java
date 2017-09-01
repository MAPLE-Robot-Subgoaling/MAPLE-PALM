package taxi.hierarchies.tasks.bringon.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import utilities.MutableObject;

public class TaxiBringonAgent extends MutableObject{
	
	/**
	 * contains if taxi is occupied and where it is (onRoad or at depot
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiBringonDomain.ATT_LOCATION
			);
	
	public TaxiBringonAgent(String name, String currentLocation) {
		this(name, (Object) currentLocation);
	}
	
	private TaxiBringonAgent(String name, Object currentLocation) {
		this.set(TaxiBringonDomain.ATT_LOCATION, currentLocation);
		this.setName(name);
	}

	@Override
	public String className() {
		return Taxi.CLASS_TAXI;
	}

	@Override
	public TaxiBringonAgent copy() {
		return (TaxiBringonAgent) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiBringonAgent(
				objectName,
				get(TaxiBringonDomain.ATT_LOCATION)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
