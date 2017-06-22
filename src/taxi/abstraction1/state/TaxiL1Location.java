package taxi.abstraction1.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.abstraction1.TaxiL1;
import utilities.MutableObject;

public class TaxiL1Location extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiL1.ATT_COLOR
			);
	
	public TaxiL1Location(String name, String color) {
		this(name, (Object) color);
	}
	
	private TaxiL1Location(String name, Object color) {
		this.set(TaxiL1.ATT_COLOR, color);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiL1.CLASS_L1LOCATION;
	}

	@Override
	public TaxiL1Location copy() {
		return (TaxiL1Location) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiL1Location(
				objectName, 
				get(TaxiL1.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
