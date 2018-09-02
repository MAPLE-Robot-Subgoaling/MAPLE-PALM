package edu.umbc.cs.maple.cleanup.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.Cleanup.Shape;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_COLOR;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_REGION;

public class StandardGoalPF extends PropositionalFunction {

    public StandardGoalPF() {
        super("goal", new String[]{});
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        CleanupState cs = (CleanupState) ooState;
        for (CleanupBlock cb : cs.getBlocks().values()){
            Object region = cb.get(ATT_REGION);
            if (region != null) {
                String regionName = region.toString();
                ObjectInstance regionObject = cs.object(regionName);
                String regionColor = (String) regionObject.get(ATT_COLOR);
                if (ShapeGoal(cb) && !regionColor.equals(cb.get(ATT_COLOR))) {
                    return false;
                }
            } else {
                String color = (String) cs.getContainingDoorOrRoom(cb).get(ATT_COLOR);
                if(ShapeGoal(cb) && !cb.get(ATT_COLOR).equals(color)){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean ShapeGoal(CleanupBlock cb){
        String shape = (String) cb.get(Cleanup.ATT_SHAPE);
        Shape sh = Shape.valueOf(shape);
        switch(sh){
            case backpack:
            case bag:
                return true;
            default:
                return false;
        }
    }
}
