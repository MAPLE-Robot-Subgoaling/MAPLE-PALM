package rmaxq.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RMAXQTaskData {

    private RmaxQLearningAgent agent;

    protected final GroundedTask task;

    protected HashMap<HashableState, RMAXQStateData> stateDataMap;

    private Set<HashableState> envelope;

    private Policy policy;

    private double gamma;

    public RMAXQTaskData(RmaxQLearningAgent agent, GroundedTask task, double gamma) {
        this.agent = agent;
        this.task = task;
        this.stateDataMap = new HashMap<>();
        this.envelope = new HashSet<>();
        this.gamma = gamma;
        this.policy = null;
    }

    public double getGamma() {
        return gamma;
    }

    public void addToEnvelope(HashableState hs) {
        envelope.add(hs);
    }

    public Set<HashableState> getEnvelope() {
        return envelope;
    }

    public RMAXQStateData getStateData(HashableState hs) {
        return this.stateDataMap.computeIfAbsent(hs, t ->
        {
            RMAXQStateData stateData;
            if (this.task.isPrimitive()) {
                stateData = new PrimitiveData(agent,this, hs);
            } else {
                stateData = new AbstractData(this, hs);
            }
            return stateData;

        });
    }

    public GroundedTask getTask() {
        return task;
    }

    public GroundedTask selectActionForState(HashableState hs) {
        GroundedTask action = policy.action(hs.s());
        return action;
    }
}

