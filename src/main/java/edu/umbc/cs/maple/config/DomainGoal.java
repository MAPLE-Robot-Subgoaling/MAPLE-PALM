package edu.umbc.cs.maple.config;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.propositional.PropositionalFunction;

import java.util.List;

public abstract class DomainGoal<T extends GoalDescription> extends PropositionalFunction implements StateConditionTest {

    protected List<T> goalDescriptions;

    public DomainGoal(){
        this("ERRORNOTSET", new String[]{});
    }

    public DomainGoal(String name, String[] params) {
        super(name, params);
    }

    public List<T> getGoalDescriptions(){
        return goalDescriptions;
    }

    public void setGoalDescriptions(List<T> goalDescriptions) {
        this.goalDescriptions = goalDescriptions;
    }

}
