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

//        CleanupState s = (CleanupState) state;
        String objectName = params[0];
//        if (params.length < 2) { return true; }
        String regionName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance region = state.object(regionName);
//        return object.get(ATT_REGION).equals(regionName);
        int x = (int) object.get(ATT_X);
        int y = (int) object.get(ATT_Y);
        int left = (int) region.get(ATT_LEFT);
        int right = (int) region.get(ATT_RIGHT);
        int bottom = (int) region.get(ATT_BOTTOM);
        int top = (int) region.get(ATT_TOP);

        return x >= left && x <= right && y >= bottom && y <= top;
    }
}
