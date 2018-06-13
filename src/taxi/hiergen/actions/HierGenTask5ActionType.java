package taxi.hiergen.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import hierarchy.framework.StringFormat;
import taxi.hiergen.TaxiHierGenState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class HierGenTask5ActionType implements ActionType {

    @Override
    public String typeName() {
        return ACTION_TASK_5;
    }

    @Override
    public Action associatedAction(String strRep) {
        String[] params = StringFormat.split(strRep);
        int goalX = Integer.parseInt(params[1]);
        int goalY = Integer.parseInt(params[2]);
        return new HierGenTask5Action(goalX, goalY);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        List<Action> actions = new ArrayList<Action>();
        TaxiHierGenState st = (TaxiHierGenState) s;
        ObjectInstance taxi = st.getTaxi();
        int tX = (int) taxi.get(ATT_X);
        int tY = (int) taxi.get(ATT_Y);
        for(ObjectInstance passenger : st.objectsOfClass(CLASS_PASSENGER)){
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
            if (passenger.get(ATT_DESTINATION_X) == null) {
                // somewhat of a hack, but allows reuse of this class for both types of conditions
                // if the attribute is null, we are invoking task5 from task7 (going to a passenger's x and y)
                // otherwise, we are invoking from root and should use the passenger's destination x and y
                if (inTaxi) {
                    continue;
                }
                // navigating to a passenger
                int pX = (int) passenger.get(ATT_X);
                int pY = (int) passenger.get(ATT_Y);
                if (tX == pX && tY == pY) {
                    // cannot nav here, already at the location
                    continue;
                }
                actions.add(new HierGenTask5Action(pX, pY));
            } else {
                if (!inTaxi) {
                    continue;
                }
                // navigating to a passenger's desired destination
                int goalX = (int) passenger.get(ATT_DESTINATION_X);
                int goalY = (int) passenger.get(ATT_DESTINATION_Y);
                if (tX == goalX && tY == goalY) {
                    // cannot nav here, already at the location
                    continue;
                }
                actions.add(new HierGenTask5Action(goalX, goalY));
            }
        }
        return actions;
    }
}
