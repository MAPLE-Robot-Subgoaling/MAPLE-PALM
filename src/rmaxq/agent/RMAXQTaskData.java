package rmaxq.agent;

import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RMAXQTaskData {

    protected final GroundedTask task;

    protected HashMap<HashableState, RMAXQStateData> stateDataMap;

    private Set<HashableState> envelope;

    private boolean computedPolicy = false;

    // "timesteps" tracker from RMAXQ algorithm, used to clear the planning envelope
    private Integer taskTimesteps;

    // the possible number of primitive actions that this task may be executed over
    private Set<Integer> possibleK;

    private double gamma;

    public RMAXQTaskData(GroundedTask task, double gamma) {
        this.task = task;
        this.stateDataMap = new HashMap<>();
        this.envelope = new HashSet<>();
        this.taskTimesteps = 0;
        this.possibleK = new HashSet<>();
        this.gamma = gamma;
    }

    public double getGamma() {
        return gamma;
    }

    public void clearTimesteps() {
        taskTimesteps = 0;
    }

    public void addToEnvelope(HashableState hs) {
        envelope.add(hs);
    }

    public Set<HashableState> getEnvelope() {
        return envelope;
    }

    public Integer getTaskTimesteps() {
        return taskTimesteps;
    }

    public void setTaskTimesteps(int taskTimesteps) {
        this.taskTimesteps = taskTimesteps;
    }

    public void addPossibleK(int k) {
        this.possibleK.add(k);
    }

    public RMAXQStateData getStateData(HashableState hs) {
        return this.stateDataMap.computeIfAbsent(hs, t ->
        {
            RMAXQStateData stateData;
            if (this.task.isPrimitive()) {
                stateData = new PrimitiveData(this, hs);
            } else {
                stateData = new AbstractData(this, hs);
            }
            return stateData;

        });
    }

    public Set<Integer> getPossibleK() {
        return possibleK;
    }

    public GroundedTask getTask() {
        return task;
    }

    public boolean isComputedPolicy() {
        return computedPolicy;
    }

    public void setComputedPolicy(boolean computedPolicy) {
        this.computedPolicy = computedPolicy;
    }

}
