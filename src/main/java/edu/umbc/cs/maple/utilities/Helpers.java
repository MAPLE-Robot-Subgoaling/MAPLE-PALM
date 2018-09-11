package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.oo.state.OOState;

public class Helpers {

    public static boolean anyParamsNull(OOState state, String[] params) {
        for (String param : params) {
            if (state.object(param) == null) {
                return true;
            }
        }
        return false;
    }

}
