package taxi.hierGen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierGen.TaxiHierGenState;
import taxi.hierGen.actions.HierGenTask5Action;
import taxi.hierGen.actions.HierGenTask5ActionType;

import static taxi.TaxiConstants.ATT_X;
import static taxi.TaxiConstants.ATT_Y;
import static taxi.TaxiConstants.PF_TASK_5;

public class HierGenTask5Completed extends PropositionalFunction {

    public HierGenTask5Completed(){
        super(PF_TASK_5, new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        //tx == goalx ty \\goaly

        TaxiHierGenState st = (TaxiHierGenState) s;
        HierGenTask5ActionType navType = new HierGenTask5ActionType();
        HierGenTask5Action action = (HierGenTask5Action) navType.associatedAction(params[0]);

        ObjectInstance taxi = st.getTaxi();
        if (taxi == null) {
            // assume if taxi does not exist, we are in imagined state, and it is terminal
            return true;
        }
        int tx = (int) taxi.get(ATT_X);
        int ty = (int) taxi.get(ATT_Y);
        int goalX = action.getGoalX();
        int goalY = action.getGoalY();

        return tx == goalX && ty == goalY;
    }
}
