package edu.umbc.cs.maple.cleanup.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.Cleanup.Shape;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class StandardGoalPF extends PropositionalFunction {

    public StandardGoalPF() {
        super("goal", new String[]{});
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        CleanupState cs = (CleanupState) ooState;
        for (CleanupBlock cb : cs.getBlocks().values()){
            String color = (String) cs.getContainingDoorOrRoom(cb).get(Cleanup.ATT_COLOR);
            if(ShapeGoal(cb) && !cb.get(Cleanup.ATT_COLOR).equals(color)){
                return false;
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
