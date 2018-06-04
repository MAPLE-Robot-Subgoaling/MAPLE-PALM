package taxi.hierGen.actions;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierGen.root.state.TaxiHierGenRootState;

import static taxi.TaxiConstants.*;

public class HierGenDropoffActiontype extends ObjectParameterizedActionType {

    public HierGenDropoffActiontype(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction a) {
        String[] params = a.getObjectParameters();
        String passengerName = params[0];

        TaxiHierGenRootState st = (TaxiHierGenRootState) s;
        int px = (int) st.getPassengerAtt(passengerName, ATT_X);
        int py = (int) st.getPassengerAtt(passengerName, ATT_Y);
        int destX = (int) st.getPassengerAtt(passengerName, TaxiHierGenRootState.ATT_DESTINAION_X);
        int destY = (int) st.getPassengerAtt(passengerName, TaxiHierGenRootState.ATT_DESTINAION_Y);
        boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, ATT_IN_TAXI);
        return /*px == destX && py == destY && */inTaxi;
    }
}
