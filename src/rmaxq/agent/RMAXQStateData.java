package rmaxq.agent;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.*;

public abstract class RMAXQStateData {

    private final RMAXQTaskData taskData;
    private final HashableState hs;

    public RMAXQStateData (RMAXQTaskData taskData, HashableState hs) {
        this.taskData = taskData;
        this.hs = hs;
    }


    public RMAXQTaskData getTaskData() {
        return taskData;
    }

    public GroundedTask getTask() {
        return taskData.getTask();
    }

    public HashableState getHs() {
        return hs;
    }

    @Override
    public String toString() {
        return getTask().toString() + " " + hs.s().toString();// hs.hashCode();
    }


    private HashMap<GroundedTask, HashMap<HashableState, State>> cachedStateMapping = new HashMap<>();
    public State getMappedState() {
        GroundedTask task = this.getTask();
        HashableState hs = this.getHs();
        Map<HashableState, State> stateMapping = cachedStateMapping.computeIfAbsent(task, i -> new HashMap<>());
        State s = stateMapping.computeIfAbsent(hs, i -> task.mapState(hs.s()));
        return s;
    }

    public abstract double getP(HashableState hsPrime);

    public abstract double getR(HashableState hsPrime);

}
