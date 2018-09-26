package edu.umbc.cs.maple.jumper;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.config.DomainGoal;
import edu.umbc.cs.maple.config.DomainGoalAll;
import edu.umbc.cs.maple.config.GoalDescription;

import java.util.ArrayList;

import static edu.umbc.cs.maple.utilities.BurlapConstants.EMPTY_ARRAY;

public class JumperGoal extends DomainGoalAll {

    public JumperGoal(PropositionalFunction pf) {
        super("JumperGoalPF", EMPTY_ARRAY);
        this.goalDescriptions = new ArrayList<>();
        this.goalDescriptions.add(new GoalDescription(pf));
    }

    @Override
    public boolean satisfies(State state) {
        throw new RuntimeException("not implemented");
    }

}
