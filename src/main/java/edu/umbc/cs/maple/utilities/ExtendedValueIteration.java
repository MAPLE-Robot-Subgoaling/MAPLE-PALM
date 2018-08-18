package edu.umbc.cs.maple.utilities;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.*;

import java.util.Map.Entry;

public class ExtendedValueIteration extends DynamicProgramming implements Planner {

    /*
    @article{auer_near-optimal_nodate,
        title = {Near-optimal {Regret} {Bounds} for {Reinforcement} {Learning}},
        language = {en},
        author = {Auer, Peter and Jaksch, Thomas and Ortner, Ronald},
        pages = {20},
        file = {Auer et al. - Near-optimal Regret Bounds for Reinforcement Learn.pdf:C\:\\Users\\Matthew\\Zotero\\storage\\QZNP7L9T\\Auer et al. - Near-optimal Regret Bounds for Reinforcement Learn.pdf:application/pdf}
    }
    */
    protected HashableStateFactory hashingFactory;
    protected ConfidenceModel model;
    protected double deltaThreshold;
    protected Set<HashableState> reachableStates;
    protected Set<Action> actions;

    protected Map<HashableState, Map<Action, Map<HashableState, Double>>> morphedTransitions;

    public ExtendedValueIteration(ConfidenceModel observedModel, Set<HashableState> states,
                                  Set<Action> actions, double delta, HashableStateFactory hsf){
        this.model = observedModel;
        this.reachableStates = states;
        this.actions = actions;
        this.deltaThreshold = delta;
        this.hashingFactory = hsf;
    }

    @Override
    public Policy planFromState(State state) {
        ValueFunction valueFn = runExtendedVI();
        return new GreedyQPolicy(this);
    }

    protected ValueFunction runExtendedVI(){
        boolean converged = false;
        int iteration = 0;
        while (!converged){
            double maxDelta = Double.MIN_VALUE;
            for(HashableState hs : reachableStates){
                double maxNewStateValue = Double.MIN_VALUE;
                for (Action a : actions){
                    morphProbabilities(hs, a);
                    Map<HashableState, Double> outTransitions = getTransitions(hs, a);
                    double weightedValueSum = 0;
                    for (HashableState hsp : outTransitions.keySet()){
                        double value = valueFunction.get(hsp);
                        weightedValueSum += outTransitions.get(hsp) * value;
                    }

                    double rewardBound = model.getRewardBound(hs, a);
                    double observedReward = model.getModelReward(hs, a);
                    double newValue = (observedReward + rewardBound) + weightedValueSum;
                    if(maxNewStateValue < newValue){
                        maxNewStateValue = newValue;
                    }
                }
                double oldValue = valueFunction.get(hs);
                double deltaValue = Math.abs(maxNewStateValue - oldValue);
                valueFunction.put(hs, maxNewStateValue);
                if(deltaValue > maxDelta){
                    maxDelta = deltaValue;
                }
            }
            converged = maxDelta < deltaThreshold;
            iteration++;
        }

        return new TabularValueFunction(hashingFactory, valueFunction, 0);
    }

    // Figure 2
    protected void morphProbabilities(HashableState hs, Action a){
        List<HashableState> states = sortStatesByValue();

        //set starting probabilities
        //TODO:  Check is the state has max value or min
        HashableState hsMax = states.get(00);
        double prob = 1;
        double optimisticTransition = model.getModelTransition(hs, a, hsMax) +
                (double)model.getTransitionBound(hs, a) / 2;
        if(optimisticTransition < 1){
            prob = optimisticTransition;
        }
        setMorphedTransition(hs, a, hsMax, prob);

        for(int i = 1; i < states.size(); i++){
            HashableState hsp = states.get(i);
            double probability = model.getModelTransition(hs, a, hsp);
            setMorphedTransition(hs, a, hsp, probability);
        }

        //morph probabilities
        Map<HashableState, Double> transitions = getTransitions(hs, a);
        int stateIdx = states.size();
        while (sumOutTransitions(transitions) > 1){
            HashableState hsp = states.get(stateIdx);
            double excludedSum = sumOutTransitions(transitions, hsp);
            double newProbability = 1 - excludedSum;
            if(newProbability < 0){
                newProbability = 0;
            }
            setMorphedTransition(hs, a, hsp, newProbability);
            stateIdx--;
        }
    }

    protected List<HashableState> sortStatesByValue(){
        Set<Entry<HashableState, Double>> set = valueFunction.entrySet();
        List<Entry<HashableState, Double>> list = new ArrayList<Entry<HashableState, Double>>(
                set);
        Collections.sort(list, new Comparator<Entry<HashableState, Double>>() {
            public int compare(Entry<HashableState, Double> o1,
                               Entry<HashableState, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<HashableState> orderedStates = new ArrayList<HashableState>();
        for(Entry e : list){
            orderedStates.add((HashableState) e.getKey());
        }

        return orderedStates;
    }

    protected void setMorphedTransition(HashableState hs, Action a, HashableState hsp, double p){

        Map<HashableState, Double> outFromS = getTransitions(hs, a);
        outFromS.put(hsp, p);
    }

    protected Map<HashableState, Double> getTransitions(HashableState hs, Action a){
        Map<Action, Map<HashableState, Double>> stateTransitions = morphedTransitions.get(hs);
        if(stateTransitions == null){
            stateTransitions = new HashMap<Action, Map<HashableState, Double>>();
            morphedTransitions.put(hs, stateTransitions);
        }

        Map<HashableState, Double> stateActionInfo = stateTransitions.get(a);
        if(stateActionInfo == null){
            stateActionInfo = new HashMap<HashableState, Double>();
            stateTransitions.put(a, stateActionInfo);
        }

        return stateActionInfo;
    }

    protected double sumOutTransitions(Map<HashableState, Double> outTransitions){
        return sumOutTransitions(outTransitions, null);
    }

    protected double sumOutTransitions(Map<HashableState, Double> outTransitions, HashableState notIncluded){
        double sum = 0;
        for (HashableState hsp : outTransitions.keySet()){
            if(notIncluded == null || !hsp.equals(notIncluded)) {
                sum += outTransitions.get(hsp);
            }
        }
        return sum;
    }
    @Override
    public void solverInit(SADomain saDomain, double v, HashableStateFactory hashableStateFactory) {

    }

    @Override
    public void resetSolver() {
        morphedTransitions.clear();
        valueFunction.clear();
        for(HashableState hs :reachableStates){
            valueFunction.put(hs, 0.);
        }
    }

    @Override
    public void setDomain(SADomain saDomain) {
        throw new RuntimeException("Solver does not use a domain");
    }

    @Override
    public void setModel(SampleModel sampleModel) { }

    @Override
    public SampleModel getModel() {
        return null;
    }

    @Override
    public Domain getDomain() {
        return null;
    }

    @Override
    public void addActionType(ActionType actionType) {

    }

    @Override
    public void setActionTypes(List<ActionType> list) {

    }

    @Override
    public List<ActionType> getActionTypes() {
        return null;
    }

    @Override
    public void setHashingFactory(HashableStateFactory hashableStateFactory) {

    }

    @Override
    public HashableStateFactory getHashingFactory() {
        return null;
    }

    @Override
    public double getGamma() {
        return 0;
    }

    @Override
    public void setGamma(double v) {

    }

    @Override
    public void setDebugCode(int i) {

    }

    @Override
    public int getDebugCode() {
        return 0;
    }

    @Override
    public void toggleDebugPrinting(boolean b) {

    }
}
