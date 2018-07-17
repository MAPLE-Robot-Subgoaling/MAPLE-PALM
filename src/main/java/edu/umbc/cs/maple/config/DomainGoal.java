package edu.umbc.cs.maple.config;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;

import java.util.List;

public abstract class DomainGoal<T extends GoalDescription> implements StateConditionTest {

    protected List<T> goalDescriptions;

    public DomainGoal(){

    }

    public List<T> getGoalDescriptions(){
        return goalDescriptions;
    }

    public void setGoalDescriptions(List<T> goalDescriptions) {
        this.goalDescriptions = goalDescriptions;
    }
}
