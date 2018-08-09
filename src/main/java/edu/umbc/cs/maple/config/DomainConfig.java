package edu.umbc.cs.maple.config;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.utilities.OOSADomainGenerator;

public abstract class DomainConfig {
    public String state;
    public OOSADomainGenerator domainGenerator;
    public abstract State generateState();
    public abstract Visualizer getVisualizer(ExperimentConfig config);

    public Domain generateDomain() {
        return domainGenerator.generateDomain();
    }

    public boolean validate() {
        if (state == null || state.equals("")) {
            return false;
        }
        if (domainGenerator == null || domainGenerator.getRf() == null || domainGenerator.getTf() == null) {
            throw new RuntimeException("Error: missing domain generator or components");
        }
        return true;
    }
}
