package edu.umbc.cs.maple.palm.ucrl.agent;

import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
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

    public PALMUCRLModelGenerator(State start, double gamma, double maxDelta, HashableStateFactory hs){
        this.start = start;
        this.gamma = gamma;
        this.maxDelta = maxDelta;
        this.hashingFactory = hs;
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        OOSADomain base = t.getDomain();
        start = t.mapState(start);
        Set<HashableState> stateSet = getStateSet(base, start);
        List<HashableState> reachableStates = new ArrayList<HashableState>(stateSet);
        return new UCRLModel(t, reachableStates, gamma, maxDelta, hashingFactory);
    }

    protected Set<HashableState> getStateSet(OOSADomain domain, State start){
        return StateReachability.getReachableHashedStates(start, domain, hashingFactory);
    }
}
