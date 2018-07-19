package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.PossibleOutcome;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateBasedRmaxModel extends HierarchicalRmaxModel {

    /**
     * creates a rmax model
     *
     * @param task              the grounded task to model
     * @param threshold         rmax sample threshold
     * @param rmax              max rewardTotal in domain
     * @param hs                provided hashing factory
     * @param gamma
     * @param useMultitimeModel
     */
    public UpdateBasedRmaxModel(Task task, int threshold, double rmax, HashableStateFactory hs, double gamma, boolean useMultitimeModel) {
        super(task, threshold, rmax, hs, gamma, useMultitimeModel);
        this.updateCounts = new HashMap<>();
    }
    private Map<HashableStateActionPair, Integer> updateCounts;

    public int getUpdateCount(State s, Action a){
        return updateCounts.getOrDefault(hashStateActionPair(s, a),0);
    }
    protected HashableStateActionPair hashStateActionPair(State s, Action a){
        HashableState hs = this.hashingFactory.hashState(s);
        String actionName = StringFormat.parameterizedActionName(a);
        HashableStateActionPair  hsap= new HashableStateActionPair(hs, actionName);
        return hsap;
    }

    public boolean batchUpdateModel(Map<HashableState, List<TimedEnvironmentOutcome>> results, String[] params){
        State s;
        Action a;
        boolean allConverged = true;
        for(Map.Entry<HashableState, List<TimedEnvironmentOutcome>> e : results.entrySet()) {
            s = e.getKey().s();
            if( e.getValue().size() < 1) continue;
            a = e.getValue().get(0).a;
            HashableStateActionPair hsap = hashStateActionPair(s, a);
            this.updateCounts.merge(hsap,1,Integer::sum);
            for(TimedEnvironmentOutcome teo : e.getValue()) {
                allConverged = updateModel(teo,teo.stepsTaken, params) && allConverged;
            }
        }
        return allConverged;
    }
    @Override
    public boolean updateModel(EnvironmentOutcome result, int stepsTaken, String[] params) {
        return super.updateModel(result, stepsTaken, params);
    }
    public static class TimedEnvironmentOutcome extends EnvironmentOutcome {
        protected int stepsTaken;
        public TimedEnvironmentOutcome(State o, Action a, State op, double r, int stepsTaken, boolean terminated) {
            super(o, a, op, r, terminated);
            this.stepsTaken = stepsTaken;
        }
    }

    @Override
    public boolean isConvergedFor(State s, Action a, State sPrime) {
        return this.getUpdateCount(s,a) >= super.mThreshold;
    }
}
