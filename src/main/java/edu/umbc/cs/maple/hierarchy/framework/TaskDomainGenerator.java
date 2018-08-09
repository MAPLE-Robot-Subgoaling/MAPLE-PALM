package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

public class TaskDomainGenerator implements DomainGenerator {
    private TerminalFunction tf;
    private RewardFunction rf;
    private StateClassPair[] stateClasses;
    private ActionType[] actions;

    public TerminalFunction getTf() {
        return tf;
    }

    public void setTf(TerminalFunction tf) {
        this.tf = tf;
    }

    public RewardFunction getRf() {
        return rf;
    }

    public void setRf(RewardFunction rf) {
        this.rf = rf;
    }

    public StateClassPair[] getStateClasses() {
        return stateClasses;
    }

    public void setStateClasses(StateClassPair[] stateClasses) {
        this.stateClasses = stateClasses;
    }

    public ActionType[] getActions() {
        return actions;
    }

    public void setActions(ActionType[] actions) {
        this.actions = actions;
    }

    @Override
    public Domain generateDomain() {
        OOSADomain domain = new OOSADomain();

        for(StateClassPair a: stateClasses){
            domain.addStateClass(a.getClassName(),a.getClasz().getClass());
        }

        for(ActionType b: actions){
            domain.addActionType(b);
        }

        return domain;
    }
}
