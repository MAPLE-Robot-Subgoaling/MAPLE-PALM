package edu.umbc.cs.maple.config;

import burlap.mdp.core.oo.state.OOState;

public abstract class DomainGoalAll extends DomainGoal<GoalDescription> {

    public DomainGoalAll(String name, String[] params) {
        super(name, params);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        for(GoalDescription gd : this.goalDescriptions) {
            boolean passes = gd.pf.isTrue(ooState, strings);
            if (!passes) { return false; }
        }
        return true;
    }

}
