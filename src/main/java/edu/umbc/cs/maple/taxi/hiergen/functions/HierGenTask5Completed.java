package edu.umbc.cs.maple.taxi.hiergen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.taxi.hiergen.TaxiHierGenState;
import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask5ActionType;
import edu.umbc.cs.maple.utilities.IntegerParameterizedAction;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class HierGenTask5Completed extends PropositionalFunction {

    public HierGenTask5Completed(){
        super(PF_TASK_5, new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        //tx == goalx ty \\goaly

        TaxiHierGenState st = (TaxiHierGenState) s;
        HierGenTask5ActionType navType = new HierGenTask5ActionType();
        IntegerParameterizedAction action = navType.associatedAction(params[0]);

        ObjectInstance taxi = st.getTaxi();
        if (taxi == null) {
            // assume if taxi does not exist, we are in imagined state, and it is terminal
            return true;
        }
        int tx = (int) taxi.get(ATT_X);
        int ty = (int) taxi.get(ATT_Y);
        int goalX = action.getIntegers()[0];
        int goalY = action.getIntegers()[1];
        return tx == goalX && ty == goalY;
    }
}
