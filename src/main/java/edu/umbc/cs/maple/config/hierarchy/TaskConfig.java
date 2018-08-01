package edu.umbc.cs.maple.config.hierarchy;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.config.solver.SolverConfig;
import edu.umbc.cs.maple.hierarchy.framework.NonprimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;

import java.util.List;

public class TaskConfig {
    List< String> children;
    DomainGenerator domainGenerator;
    StateMapping stateMapper;
    TerminalFunction tf;
    RewardFunction rf;
    SolverConfig solver;
    Domain domain;
    public TaskConfig(){}

    public Domain buildDomain(){
        domain = domainGenerator.generateDomain();
        solver.setDomain((SADomain) domain);
        return domain;
    }
    public Task finalizeTask(List<Task> kids, ActionType actionType){
        return new NonprimitiveTask(
                kids.toArray(new Task[kids.size()]),
                actionType,
                (OOSADomain) domain,
                stateMapper,
                tf,
                rf,
                solver
        );
    }



    public List< String> getChildren() {
        return children;
    }
    public void setChildren(List<String> children) {
        this.children = children;
    }
    public DomainGenerator getDomainGenerator() {
        return domainGenerator;
    }
    public void setDomainGenerator(DomainGenerator domain) {
        this.domainGenerator = domain;
    }
    public StateMapping getStateMapper() {
        return stateMapper;
    }
    public void setStateMapper(StateMapping stateMapping) {
        this.stateMapper = stateMapping;
    }
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
    public SolverConfig getSolver() {
        return solver;
    }
    public void setSolver(SolverConfig solver) {
        this.solver = solver;
    }
}
