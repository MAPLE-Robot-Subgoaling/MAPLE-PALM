package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import edu.umbc.cs.maple.config.DomainGoal;

public abstract class AMDPRootGoalPF extends PropositionalFunction {
    protected DomainGoal goal;

    public AMDPRootGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public DomainGoal getGoal() {
        return goal;
    }

    public void setGoal(DomainGoal goal) {
        this.goal = goal;
    }
}
