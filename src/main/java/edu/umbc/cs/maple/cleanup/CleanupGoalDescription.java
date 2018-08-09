package edu.umbc.cs.maple.cleanup;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import edu.umbc.cs.maple.config.GoalDescription;

public class CleanupGoalDescription extends GoalDescription {

    public CleanupGoalDescription(){}
    public CleanupGoalDescription(String[] params, PropositionalFunction pf){
        super(params, pf);
    }

}
