package edu.umbc.cs.maple.palm.ucb.agent;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.utilities.ConstantDiscountProvider;
import edu.umbc.cs.maple.utilities.DiscountProvider;

import java.util.ArrayList;
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
        }0
     */

    // n(s,a), n(s, a, s') - total counts
    protected Map<HashableState, Map<Action, Integer>> totalStateAction;
    protected Map<HashableState, Map<Action, Map<HashableState, Integer>>> totalStateActionState;
    protected Map<HashableState, Map<Action, Double>> totalReward;

    // v(s, a), v(s, a, s') - batch counts
    protected Map<HashableState, Map<Action, Integer>> batchStateAction;
    protected Map<HashableState, Map<Action, Map<HashableState, Integer>>> batchStateActionState;
    protected Map<HashableState, Map<Action, Double>> batchReward;

    // current model
    protected Map<HashableState, Map<Action, Double>> rewards;
    protected Map<HashableState, Map<Action, Map<HashableState, Double>>> transitionProbabilities;

    protected HashableState imaginedState;
    protected double rmax;
    protected double gamma;
    protected double epsilon;

    //t
    protected int timestep = 0;

    //k
    protected int batchCount = 0;

    protected int actionDelay = 0;
    protected boolean delaying;
    protected int H_delay;

    protected HashableStateFactory hashingFactory;

    protected DiscountProvider discountProvider;

    protected GroundedTask task;
    protected TerminalFunction tf;
    //knownness constants

    protected int MAG_S;
    protected int MAG_A;
    protected double W_MIN;
    protected int L_MAX;
    protected List<Integer> K_SET;
    protected double m;
    protected double l_1;
    protected double delta_1;
    protected double delta;
    protected int U_max
    protected int beta;

    public UCBModel(double gamma, double rmax, double epsilon, HashableStateFactory hashableStateFactory){
        this.initializeDiscountProvider(gamma);
        this.gamma = gamma;
        this.rmax = rmax;
        this.epsilon = epsilon;
        this.hashingFactory = hashableStateFactory;
        this.delaying = false;
        defineConstants();
    }

    public UCBModel(GroundedTask task, double gamma, double rmax, double epsilon, HashableStateFactory hashableStateFactory){
        this(gamma, rmax, epsilon, hashableStateFactory);
        this.task = task;
    }

    public UCBModel(TerminalFunction tf, double gamma, double rmax, double epsilon, HashableStateFactory hashableStateFactory){
        this(gamma, rmax, epsilon, hashableStateFactory);
        this.tf = tf;
    }

    public void initializeDiscountProvider(double gamma) {
        this.discountProvider = new ConstantDiscountProvider(gamma);
    }

    @Override
    public boolean terminal(State s) {
        if(task != null) {
            return task.isFailure(s) || task.isComplete(s);
        }else {
            return tf.isTerminal(s);
        }
    }

    //TODO figure out where main loop in algo #1 goes
    @Override
    public List<TransitionProb> transitions(State s, Action a) {
        if(!delaying){
            //set converged values in the model
            boolean noUsefulBatchInfo = checkKnowness();
            if(!noUsefulBatchInfo){
                // there is a transition which the current batch has info about
                delaying = true;
                actionDelay = H_delay;
            }
        }

        List<TransitionProb> tps = new ArrayList<TransitionProb>();
        HashableState hs = hashingFactory.hashState(s);
        if(! isConfident(hs, a)) {
            tps.add(createImaginedTransition(hs, a));
            return tps;
        }

        Map<HashableState, Double> outcomes = getPossibleOutcomes(hs, a);
        Double r = getModelReward(hs, a);
        for(HashableState hsp : outcomes.keySet()){
            Double p = getModelTransition(hs, a, hsp);
            EnvironmentOutcome eo = new EnvironmentOutcome(s, a, hsp.s(), r, terminal(hsp.s()));
            tps.add(new TransitionProb(p, eo));
        }
        return tps;
    }

    @Override
    public void updateModel(EnvironmentOutcome result, int stepsTaken) {
        //act from cite
        HashableState hs = hashingFactory.hashState(result.o);
        Action a = result.a;
        double reward = result.r;
        HashableState hsp = hashingFactory.hashState(result.op);

        incrementBatchStateAction(hs, a);
        incrementBatchStateActionState(hs, a, hsp);
        addBatchReward(hs, a, reward);

        timestep++;
        if(delaying){
            actionDelay--;
            if(actionDelay == 0){
                batchUpdate();
                delaying = false;
            }
        }
    }

    protected void batchUpdate(){
        // Update from cite
        for (HashableState hs : batchStateAction.keySet()){
            for (Action a : batchStateAction.get(hs).keySet()){
                int prevCount = getTotalStateAction(hs, a);
                int batchCount = getBatchStateAction(hs, a);
                setTotalStateAction(hs, a, prevCount + batchCount );

                double prevRewardTotal = getTotalSAReward(hs, a);
                double batchRewardTotal = getBatchReward(hs, a);
                setTotalReward(hs, a, prevRewardTotal + batchRewardTotal);

                for(HashableState hsp : batchStateActionState.get(hs).get(a).keySet()){
                    prevCount = getTotalStateActionState(hs, a, hsp);
                    batchCount = getBatchStateActionState(hs, a, hsp);
                    setTotalStateActionState(hs, a, hsp, prevCount + batchCount);
                }
            }
        }
        updateConvergedTransitions();
        batchCount++;
    }

    protected void updateConvergedTransitions(){
        for(HashableState hs : totalStateAction.keySet()){
            Map<Action, Map<HashableState, Integer>> stateInfo = totalStateActionState.get(hs);
            for (Action a : stateInfo.keySet()){
                if(isConfident(hs, a)){
                    double totReward = getTotalSAReward(hs, a);
                    int saCount = getBatchStateAction(hs, a);
                    double avgReward = (double) totReward / saCount;
                    setModelReward(hs, a, avgReward);

                    Map<HashableState, Integer> possibleOutcomes = stateInfo.get(a);
                    for(HashableState hsp : possibleOutcomes.keySet()){
                        double totsasCount = getTotalStateActionState(hs, a, hsp);
                        double avgTransition = (double) totsasCount / saCount;
                        setModelTransition(hs, a, hsp, avgTransition);
                    }
                }
            }
        }


    }
    //TODO: Implement formula from defn #1
    protected int knownness(int levrl, int known){

    }

    protected boolean checkKnowness(){
        for(HashableState hs : totalStateAction.keySet()) {
            Map<Action, Map<HashableState, Integer>> stateInfo = totalStateActionState.get(hs);
            for (Action a : stateInfo.keySet()) {
                int totSACount = getTotalStateAction(hs, a);
                int batchSACount = getBatchStateAction(hs, a);
                for (int level = 0; level <= L_MAX; level++){
                    int batchTotKnowness = knownness(level, totSACount + batchSACount);
                    int totKnowness = knownness(level, totSACount);
                    if(batchTotKnowness != totKnowness){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //TODO: Define confidwence in transition
    protected boolean isConfident(HashableState hs, Action a){

    }

    //TODO: See how UCRL handles "convergence"
    @Override
    public boolean isConvergedFor(State s, Action a, State sPrime) {
        HashableState hs = hashingFactory.hashState(s);
        return isConfident((hs, a));
    }


    @Override
    public DiscountProvider getDiscountProvider() {
        return discountProvider;
    }

    protected void defineConstants(){
        this.MAG_S = ;

        this.W_MIN = epsilon * (1 - gamma) / 4 * ((double) MAG_S);

        this.L_MAX = (int) Math.ceil((1 / Math.log(2)) * ( (8 * MAG_S) / (epsilon * Math.pow(1 - gamma, 2))));

        this.K_SET = new ArrayList<Integer>();
        for(int i = 0; i <= MAG_S; i++){
            this.K_SET.add(i);
            this.K_SET.add(-1 * i);
        }

        this.H_delay = (int) Math.ceil((1 / (1 - gamma)) * Math.log(8 * MAG_S / (epsilon * (1 - gamma))));

        this.U_max = MAG_S * MAG_A * K_SET.size() * (L_MAX + 1)
        this.beta = (int) Math.ceil(1 / (2 * Math.log(2)) * Math.log(1 / (1-gamma)));
        this.delta_1 = delta / ((2 * MAG_S * MAG_A) * U_max);
        this.l_1 = Math.log(2 / delta_1);
        this.m = (20 * l_1 * K_SET.size() * (L_MAX + 1) * Math.pow(2 * beta + 1, 2)) / (Math.pow(epsilon, 2) * Math.pow(1 - gamma, 2 + 2 / beta));
    }

    protected TransitionProb createImaginedTransition(HashableState hs, Action a){
        if(imaginedState == null){
            createImaginedState(hs);
        }
        EnvironmentOutcome eo = new EnvironmentOutcome(hs.s(), a, imaginedState.s(), rmax, false);
        TransitionProb tp = new TransitionProb(1., eo);
        return tp;
    }

    //________________________________________Getters and Setters_______________________

    // getters
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

    protected HashableState createImaginedState(HashableState basis) {
        MutableOOState imaginedState = (MutableOOState) basis.s().copy();
        List<ObjectInstance> objectInstances = new ArrayList<ObjectInstance>(imaginedState.objects());
        for (ObjectInstance objectInstance : objectInstances) {
            imaginedState.removeObject(objectInstance.name());
        }
        HashableState hImaginedState = hashingFactory.hashState(imaginedState);
        this.imaginedState = hImaginedState;
        return hImaginedState;
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

    // The reward starts at rmax until there is enough confidence
    protected double getTotalSAReward(HashableState hs, Action a){
        Map<Action, Double> stateActionss = totalReward.get(hs);
        if(stateActionss == null){
            stateActionss = new HashMap<Action, Double>();
            totalReward.put(hs, stateActionss);
        }

        Double reward = stateActionss.get(a);
        if(reward == null){
            reward = 0.;
        }
        return reward;
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
            stateInfo.put(a, count);
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

    protected double getBatchReward(HashableState hs, Action a){
        Map<Action, Double> stateRewards = batchReward.get(hs);
        if(stateRewards == null){
            stateRewards = new HashMap<Action, Double>();
            batchReward.put(hs, stateRewards);
        }

        Double reward = stateRewards.get(a);
        if(reward == null){
            reward = 0.;
        }
        return reward;
    }

    // current model
    protected double getModelReward(HashableState hs, Action a){
        Map<Action, Double> stateRewards = rewards.get(hs);
        if(stateRewards == null){
            stateRewards = new HashMap<Action, Double>();
            rewards.put(hs, stateRewards);
        }

        Double reward = stateRewards.get(a);
        if(reward == null){
            reward = rmax;
        }
        return reward;
    }

    protected double getModelTransition(HashableState hs, Action a, HashableState hsp){
        Map<Action, Map<HashableState, Double>> stateTransitions = transitionProbabilities.get(hs);
        if(stateTransitions == null){
            stateTransitions = new HashMap<Action, Map<HashableState, Double>>();
            transitionProbabilities.put(hs, stateTransitions);
        }

        Map<HashableState, Double> stateActionInfo = stateTransitions.get(a);
        if(stateActionInfo == null){
            stateActionInfo = new HashMap<HashableState, Double>();
            stateTransitions.put(a, stateActionInfo);
        }

        Double p = stateActionInfo.get(hsp);
        if(p == null){
            p = 0.;
            stateActionInfo.put(hsp, p);
        }
        return p;
    }
    protected Map<HashableState, Double> getPossibleOutcomes(HashableState hs, Action a){
        Map<Action, Map<HashableState, Double>> stateInfo = transitionProbabilities.get(hs);
        if(stateInfo == null){
            stateInfo = new HashMap<Action, Map<HashableState, Double>>();
            transitionProbabilities.put(hs, stateInfo);
        }

        Map<HashableState, Double> stateActionInfo = stateInfo.get(a);
        if(stateActionInfo == null){
            stateActionInfo = new HashMap<HashableState, Double>();
            stateInfo.put(a, stateActionInfo);
        }
        return stateActionInfo;
    }

    //setters

    protected void setModelReward(HashableState hs, Action a, double reward){
        getModelReward(hs, a);
        rewards.get(hs).put(a, reward);
    }

    protected void setModelTransition(HashableState hs, Action a, HashableState hsp, double probability){
        getModelTransition(hs, a, hsp);
        transitionProbabilities.get(hs).get(a).put(hsp, probability);
    }

    protected void incrementBatchStateAction(HashableState hs, Action a){
        Integer count = getBatchStateAction(hs, a);
        setBatchStateAction(hs, a, count + 1);
    }

    protected void incrementBatchStateActionState(HashableState hs, Action a, HashableState hsp){
        Integer count = getBatchStateActionState(hs, a, hsp);
        setBatchStateActionState(hs, a, hsp, count + 1);
    }

    protected void addBatchReward(HashableState hs, Action a, double increase){
        Double reward = getBatchReward(hs, a);
        setBatchReward(hs, a, reward + increase);
    }

    protected void setTotalStateAction(HashableState hs, Action a, Integer count){
        getTotalStateAction(hs, a);
        totalStateAction.get(hs).put(a, count);
    }

    protected void setTotalStateActionState(HashableState hs, Action a, HashableState hsp, Integer count){
        getTotalStateActionState(hs, a, hsp);
        totalStateActionState.get(hs).get(a).put(hsp, count);
    }

    protected void setTotalReward(HashableState hs, Action a, Double r){
        getTotalSAReward(hs, a);
        totalReward.get(hs).put(a, r);
    }

    protected void setBatchStateAction(HashableState hs, Action a, Integer count){
        getBatchStateAction(hs, a);
        batchStateAction.get(hs).put(a, count);
    }

    protected void setBatchStateActionState(HashableState hs, Action a, HashableState hsp, Integer count){
        getBatchStateActionState(hs, a, hsp);
        batchStateActionState.get(hs).get(a).put(hsp, count);
    }

    protected void setBatchReward(HashableState hs, Action a, Double r){
        getBatchReward(hs, a);
        batchReward.get(hs).put(a, r);
    }
}
