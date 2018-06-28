package edu.umbc.cs.maple.palm.ucb.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import com.sun.org.apache.regexp.internal.RE;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.utilities.ConstantDiscountProvider;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import org.omg.PortableInterceptor.ACTIVE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UCBModel extends PALMModel {

    /*
        Implementation of belop paper's Algorithm 1 writen by Matthew Landen

        @article{lattimore_pac_2012,
            title = {{PAC} {Bounds} for {Discounted} {MDPs}},
            url = {http://arxiv.org/abs/1202.3890},
            language = {en},
            urldate = {2018-06-28},
            journal = {arXiv:1202.3890 [cs]},
            author = {Lattimore, Tor and Hutter, Marcus},
            month = feb,
            year = {2012},
            note = {arXiv: 1202.3890},
            keywords = {Computer Science - Machine Learning},
        }
     */

    // n(s,a), n(s, a, s') - total counts
    protected Map<HashableState, Map<Action, Integer>> totalStateAction;
    protected Map<HashableState, Map<Action, Map<HashableState, Integer>>> totalStateActionState;

    // v(s, a), v(s, a, s') - batch counts
    protected Map<HashableState, Map<Action, Integer>> batchStateAction;
    protected Map<HashableState, Map<Action, Map<HashableState, Integer>>> batchStateActionState;

    //t
    protected int timestep = 0;

    //k
    protected int batchCount = 0;

    protected HashableStateFactory hashingFactory;

    protected DiscountProvider discountProvider;

    protected GroundedTask task;

    public UCBModel(GroundedTask task, double gamma, HashableStateFactory hashableStateFactory){
        this.task = task;
        this.initializeDiscountProvider(gamma);
        this.hashingFactory = hashableStateFactory;
    }

    public void initializeDiscountProvider(double gamma) {
        this.discountProvider = new ConstantDiscountProvider(gamma);
    }

    @Override
    public boolean terminal(State s) {
        return task.isFailure(s) || task.isComplete(s);
    }

    //TODO figure out where main loop in algo #1 goes
    @Override
    public List<TransitionProb> transitions(State s, Action a) {

    }

    @Override
    public void updateModel(EnvironmentOutcome result, int stepsTaken) {
        //act from cite
        HashableState hs = hashingFactory.hashState(result.o);
        Action a = result.a;
        double reward = result.r;
        HashableState hsp = hashingFactory.hashState(result.op);

        incrementBatchStateActin(hs, a);
        incrementBatchStateActionState(hs, a, hsp);
        timestep++;
    }

    protected void batchUpdate(){
        // Update from cite
        for (HashableState hs : batchStateAction.keySet()){
            for (Action a : batchStateAction.get(hs).keySet()){
                int prevCount = getTotalStateAction(hs, a);
                int batchCount = getBatchStateAction(hs, a);
                setTotalStateAction(hs, a, prevCount + batchCount );

                for(HashableState hsp : batchStateActionState.get(hs).get(a).keySet()){
                    prevCount = getTotalStateActionState(hs, a, hsp);
                    batchCount = getBatchStateActionState(hs, a, hsp);
                    setBatchStateActionState(hs, a, hsp, prevCount + batchCount);
                }
            }
        }
        batchCount++;
    }

    //TODO: Implement formula from defn #1
    protected int knownness(int i, int n){

    }

    //TODO: See how UCRL handles "convergence"
    @Override
    public boolean isConvergedFor(State s, Action a, State sPrime) {
        return false;
    }


    @Override
    public DiscountProvider getDiscountProvider() {
        return discountProvider;
    }

    //getters
    protected int getTotalStateAction(HashableState hs, Action a){
        Map<Action, Integer> stateInfo = totalStateAction.get(hs);
        if(stateInfo == null){
            stateInfo = new HashMap<Action, Integer>();
            totalStateAction.put(hs, stateInfo);
        }

        Integer count = stateInfo.get(a);
        if(count == null){
            count = 0;
            stateInfo.put(a, count);
        }
        return count;
    }

    protected int getTotalStateActionState(HashableState hs, Action a, HashableState hsp){
        Map<Action, Map<HashableState, Integer>> stateInfo = totalStateActionState.get(hs);
        if(stateInfo == null){
            stateInfo = new HashMap<Action, Map<HashableState, Integer>>();
            totalStateActionState.put(hs, stateInfo);
        }

        Map<HashableState, Integer> stateActionInfo = stateInfo.get(a);
        if(stateActionInfo == null){
            stateActionInfo = new HashMap<HashableState, Integer>();
            stateInfo.put(a, stateActionInfo);
        }

        Integer count = stateActionInfo.get(hsp):
        if(count == null){
            count = 0;
            stateActionInfo.put(hsp, count);
        }
        return count;
    }

    protected int getBatchStateAction(HashableState hs, Action a){
        Map<Action, Integer> stateInfo = batchStateAction.get(hs);
        if(stateInfo == null){
            stateInfo = new HashMap<Action, Integer>();
            batchStateAction.put(hs, stateInfo);
        }

        Integer count = stateInfo.get(a);
        if(count == null){
            count = 0;
            stateInfo.put(a, count)
        }
        return count;
    }

    protected int getBatchStateActionState(HashableState hs, Action a, HashableState hsp){
        Map<Action, Map<HashableState, Integer>> stateInfo = batchStateActionState.get(hs);
        if(stateInfo == null){
            stateInfo = new HashMap<Action, Map<HashableState, Integer>>();
            batchStateActionState.put(hs, stateInfo);
        }

        Map<HashableState, Integer> stateActionInfo = stateInfo.get(a);
        if(stateActionInfo == null){
            stateActionInfo = new HashMap<HashableState, Integer>();
            stateInfo.put(a, stateActionInfo);
        }

        Integer count = stateActionInfo.get(hsp);
        if(count == null){
            count = 0;
            stateActionInfo.put(hsp, count);
        }
        return count;
    }

    //setters
    protected void incrementBatchStateActin(HashableState hs, Action a){
        Integer count = getBatchStateAction(hs, a);
        setBatchStateAction(hs, a, count + 1);
    }

    protected void incrementBatchStateActionState(HashableState hs, Action a, HashableState hsp){
        Integer count = getBatchStateActionState(hs, a, hsp);
        setBatchStateActionState(hs, a, hsp, count + 1);
    }

    protected void setTotalStateAction(HashableState hs, Action a, Integer count){
        getTotalStateAction(hs, a);
        totalStateAction.get(hs).put(a, count);
    }

    protected void setTotalStateActionState(HashableState hs, Action a, HashableState hsp, Integer count){
        getTotalStateActionState(hs, a, hsp);
        totalStateActionState.get(hs).get(a).put(hsp, count);
    }

    protected void setBatchStateAction(HashableState hs, Action a, Integer count){
        getBatchStateAction(hs, a);
        batchStateAction.get(hs).put(a, count);
    }

    protected void setBatchStateActionState(HashableState hs, Action a, HashableState hsp, Integer count){
        getBatchStateActionState(hs, a, hsp);
        batchStateActionState.get(hs).get(a).put(hsp, count);
    }
}
