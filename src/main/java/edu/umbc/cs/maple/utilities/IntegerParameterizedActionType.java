package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;

import java.util.Arrays;
import java.util.List;

public abstract class IntegerParameterizedActionType implements ActionType {

    public static final String PARAMETER_ORDER_PREFIX = "P";

    protected String name;
    protected String[] parameterOrderGroups;

    public IntegerParameterizedActionType(String name, int parameterCount) {
        this(name, createParameterOrderGroupArray(parameterCount));
    }

    public IntegerParameterizedActionType(String name, String[] parameterOrderGroups) {
        this.name = name;
        this.parameterOrderGroups = parameterOrderGroups;
    }

    @Override
    public String typeName() {
        return name;
    }

    @Override
    public IntegerParameterizedAction associatedAction(String strRep) {
        String[] params = StringFormat.split(strRep);
        int removeFirstParam = 1; // skip the name of the action
        int[] intParams = Arrays.stream(params).skip(removeFirstParam).mapToInt(Integer::parseInt).toArray();
        return new IntegerParameterizedAction(name, intParams);
    }

    public static String[] createParameterOrderGroupArray(int parameterCount) {
        String[] parameterOrderGroup = new String[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            parameterOrderGroup[i] = PARAMETER_ORDER_PREFIX + i;
        }
        return parameterOrderGroup;
    }

    public IntegerParameterizedAction createAction(int... params) {
        return new IntegerParameterizedAction(name, params);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getParameterOrderGroups() {
        return parameterOrderGroups;
    }

    public void setParameterOrderGroups(String[] parameterOrderGroups) {
        this.parameterOrderGroups = parameterOrderGroups;
    }

}
