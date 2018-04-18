package utilities;

import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.Map;

public class TabularValueFunction implements ValueFunction {

    private HashableStateFactory hashingFactory;
    private Map<HashableState, Double> valueTable;
    private double defaultValue;

    public TabularValueFunction(HashableStateFactory hashingFactory, Map<HashableState, Double> valueTable, double defaultValue) {
        this.hashingFactory = hashingFactory;
        this.valueTable = valueTable;
        this.defaultValue = defaultValue;
    }

    @Override
    public double value(State state) {
        HashableState hs = hashingFactory.hashState(state);
        if (!valueTable.containsKey(hs)) {
            return defaultValue;
        }
        return valueTable.get(hs);
    }
}
