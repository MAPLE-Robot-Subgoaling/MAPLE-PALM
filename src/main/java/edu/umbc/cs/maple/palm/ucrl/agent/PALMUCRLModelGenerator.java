package edu.umbc.cs.maple.palm.ucrl.agent;

import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PALMUCRLModelGenerator implements PALMModelGenerator {

    protected double gamma;
    protected HashableStateFactory hashingFactory;
    protected double maxDelta;
    protected State start;
    protected Task root;
    protected Set<HashableState> baseStates;

    public PALMUCRLModelGenerator(Task root, State start, double gamma, double maxDelta, HashableStateFactory hs){
        this.start = start;
        this.gamma = gamma;
        this.maxDelta = maxDelta;
        this.hashingFactory = hs;
        GroundedTask groot = root.getAllGroundedTasks(start).get(0);
        baseStates = getStateSet(groot);
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        start = t.mapState(start);
        List<HashableState> reachableStates = new ArrayList<HashableState>(baseStates);
        return new UCRLModel(t, reachableStates, gamma, maxDelta, hashingFactory);
    }

    protected Set<HashableState> getStateSet(GroundedTask groot){
        OOSADomain base = getBaseDomain(groot, start);
        return StateReachability.getReachableHashedStates(start, base, hashingFactory);
    }

    protected OOSADomain getBaseDomain(GroundedTask t, State s){
        State abstractState = t.mapState(s);
        for(GroundedTask gtChild : t.getGroundedChildTasks(abstractState)){
            OOSADomain domain = gtChild.getDomain();
            if(domain.getModel() instanceof FullModel) {
                return domain;
            }else {
                return getBaseDomain(gtChild, s);
            }
        }
        throw new RuntimeException("No base domain");
    }
}
