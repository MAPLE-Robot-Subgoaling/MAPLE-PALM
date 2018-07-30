package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class ObjectNextToObjectGoalPF extends PropositionalFunction {

    public ObjectNextToObjectGoalPF(){
        super("move", new String[]{});
    }
    public ObjectNextToObjectGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState ooState, String[] params) {
        //when object1 is next to object 2
        String object1Name = params[0];
        String object2Name = params[1];
        ObjectInstance object1 = ooState.object(object1Name);
        ObjectInstance object2 = ooState.object(object2Name);
        int x1 = (int) object1.get(ATT_X);
        int y1 = (int) object1.get(ATT_Y);
        int x2 = (int) object2.get(ATT_X);
        int y2 = (int) object2.get(ATT_Y);
        //object1 +/- 1 fom object2 coordinates
        if((x1+1==x2||x1-1==x2)&&(y1+1==y2||y1-1==y2)){
            return true;
        }
        return false;
    }
}
