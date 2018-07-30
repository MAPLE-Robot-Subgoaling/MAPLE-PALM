package edu.umbc.cs.maple.config;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.utilities.OOSADomainGenerator;

public abstract class DomainConfig {
    public String state;
    private OOSADomainGenerator domainGenerator;
    public abstract State generateState();
    public abstract Visualizer getVisualizer(ExperimentConfig config);
    protected abstract OOSADomainGenerator initializeDomainGenerator();
    public Domain generateDomain(DomainGoal goal) {
        domainGenerator = initializeDomainGenerator();
        GoalFailTF tf = new GoalFailTF(goal);
        GoalFailRF rf = new GoalFailRF(tf);
        domainGenerator.setTf(tf);
        domainGenerator.setRf(rf);
        return domainGenerator.generateDomain();
    }


    public boolean validate() {
        if (state == null || state.equals("")) {
            return false;
        }
        return true;
    }
}
