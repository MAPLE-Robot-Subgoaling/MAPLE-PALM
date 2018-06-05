package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import hierarchy.framework.StringFormat;
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
        OOState st = (OOState) s;
        for(ObjectInstance passenger : st.objectsOfClass(CLASS_PASSENGER)){
            if (passenger.get(ATT_DESTINATION_X) == null) {
                // somewhat of a hack, but allows reuse of this class for both types of conditions
                // if the attribute is null, we are invoking Task5 from Task7 (going to a passenger's x and y)
                // otherwise, we are invoking from root and should use the passenger's destination x and y
                boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
                if (inTaxi) {
                    continue;
                }
                int pX = (int) passenger.get(ATT_X);
                int pY = (int) passenger.get(ATT_Y);
                actions.add(new HierGenTask5Action(pX, pY));
                return actions;
            }
            int goalX = (int) passenger.get(ATT_DESTINATION_X);
            int goalY = (int) passenger.get(ATT_DESTINATION_Y);
            actions.add(new HierGenTask5Action(goalX, goalY));
        }
        return actions;
    }
}
