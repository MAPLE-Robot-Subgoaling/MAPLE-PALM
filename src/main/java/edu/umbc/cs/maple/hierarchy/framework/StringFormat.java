package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;

public class StringFormat {

    public static final String PARAMETER_DELIMITER = " ";

    public static String parameterizedActionName(Action action) {
        String actionName = action.actionName();
        if (action instanceof ObjectParameterizedAction) {
            ObjectParameterizedAction opa = (ObjectParameterizedAction)action;
            actionName = StringFormat.join(actionName, opa.getObjectParameters());
        }
        return actionName;
    }

    public static String join(String first, String... rest) {
        return first + PARAMETER_DELIMITER + String.join(PARAMETER_DELIMITER, rest);
    }

    public static String[] split(String strRep) {
        return strRep.split(PARAMETER_DELIMITER);
    }


}
