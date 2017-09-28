package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import cleanup.state.CleanupState;

import static cleanup.Cleanup.*;

public class ObjectInRegionGoalPF extends PropositionalFunction {

    public ObjectInRegionGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        String objectName = params[0];
        String regionName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance region = state.object(regionName);
        int x = (int) object.get(ATT_X);
        int y = (int) object.get(ATT_Y);
        int left = (int) region.get(ATT_LEFT);
        int right = (int) region.get(ATT_RIGHT);
        int bottom = (int) region.get(ATT_BOTTOM);
        int top = (int) region.get(ATT_TOP);
        //special case, 1-cell region
        if (x == left && x == right && y == bottom && y == top) {
            return true;
        } else {
            //otherwise, must fit within the bounds
            return x > left && x < right && y > bottom && y < top;
        }
    }
}
