package edu.umbc.cs.maple.utilities;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.*;


/**
 * An implementation of asynchronous value iteration. Values of states are updated using the Bellman operator in an arbitrary order and a complete pass
 * over the state space is performed on each iteration. VI can be set to terminate under two possible conditions: when the maximum change in the value
 * function is smaller than some threshold or when a threshold of iterations is passed. This implementation first determines the state space by finding
 * all reachable states from a source state. The worst case time complexity of the reachability operation is equivalent to that of one VI iteration and has the added benefit
 * that VI does not pass over non-reachable states.
 *
 * This implementation is compatible with options.
 *
 *
 * @author James MacGlashan
 *
 */
public class ValueIterationMultiStep extends DynamicProgrammingMultiStep implements Planner {

    /**
     * When the maximum change in the value function is smaller than this value, VI will terminate.
     */
    protected double												maxDelta;

    /**
     * When the number of VI iterations exceeds this value, VI will terminate.
     */
    protected int													maxIterations;


    /**
     * Indicates whether the reachable states has been computed yet.
     */
    protected boolean												foundReachableStates = false;


    /**
     * When the reachability analysis to find the state space is performed, a breadth first search-like pass
     * (spreading over all stochastic transitions) is performed. It can optionally be set so that the
     * search is pruned at terminal states by setting this value to true. By default, it is false and the full
     * reachable state space is found
     */
    protected boolean												stopReachabilityFromTerminalStates = false;


    protected boolean												hasRunVI = false;


    /**
     * Initializers the valueFunction.
     * @param domain the domain in which to plan
     * @param hashingFactory the state hashing factor to use
     * @param maxDelta when the maximum change in the value function is smaller than this value, VI will terminate.
     * @param maxIterations when the number of VI iterations exceeds this value, VI will terminate.
     */
    public ValueIterationMultiStep(SADomain domain, HashableStateFactory hashingFactory, double maxDelta, int maxIterations, DiscountProvider discountProvider){
        this.DPPInit(domain, hashingFactory, discountProvider);
        this.maxDelta = maxDelta;
        this.maxIterations = maxIterations;
    }


    /**
     * Calling this method will force the valueFunction to recompute the reachable states when the {@link #planFromState(State)} method is called next.
     * This may be useful if the transition dynamics from the last planning call have changed and if planning needs to be restarted as a result.
     */
    public void recomputeReachableStates(){
        this.foundReachableStates = false;
    }


    /**
     * Sets whether the state reachability search to generate the state space will be prune the search from terminal states.
     * The default is not to prune.
     * @param toggle true if the search should prune the search at terminal states; false if the search should find all reachable states regardless of terminal states.
     */
    public void toggleReachabiltiyTerminalStatePruning(boolean toggle){
        this.stopReachabilityFromTerminalStates = toggle;
    }


    /**
     * Plans from the input state and then returns a {@link burlap.behavior.policy.GreedyQPolicy} that greedily
     * selects the action with the highest Q-value and breaks ties uniformly randomly.
     * @param initialState the initial state of the planning problem
     * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
     */
    @Override
    public GreedyQPolicy planFromState(State initialState){
        if(this.performReachabilityFrom(initialState) || !this.hasRunVI){
            this.runVI();
        }

        return new GreedyQPolicy(this);
    }

    @Override
    public void resetSolver(){
        super.resetSolver();
        this.foundReachableStates = false;
        this.hasRunVI = false;
    }

    /**
     * Runs VI until the specified termination conditions are met. In general, this method should only be called indirectly through the {@link #planFromState(State)} method.
     * The {@link #performReachabilityFrom(State)} must have been performed at least once
     * in the past or a runtime exception will be thrown. The {@link #planFromState(State)} method will automatically call the {@link #performReachabilityFrom(State)}
     * method first and then this if it hasn't been run.
     */
    public void runVI(){

        if(!this.foundReachableStates){
            throw new RuntimeException("Cannot run VI until the reachable states have been found. Use the planFromState or performReachabilityFrom method at least once before calling runVI.");
        }

        Set <HashableState> states = valueFunction.keySet();
        if (states.size() < 1) {
            System.err.println("no reachable states");
//            throw new RuntimeException("Error: no reachable states found");
        }

        int i;
        for(i = 0; i < this.maxIterations; i++){

            double delta = 0.;
            for(HashableState sh : states){

                double v = this.value(sh);
                double maxQ = this.performBellmanUpdateOn(sh);
                delta = Math.max(Math.abs(maxQ - v), delta);

            }

            if(delta < this.maxDelta){
                break; //approximated well enough; stop iterating
            }

        }

        if (i >= maxIterations) {
            System.err.println("ValueIteration exhausted its planning budget ... ");
        }

        this.hasRunVI = true;

    }


    /**
     * This method will find all reachable states that will be used by the {@link #runVI()} method and will cache all the transition dynamics.
     * This method will not do anything if all reachable states from the input state have been discovered from previous calls to this method.
     * @param si the source state from which all reachable states will be found
     * @return true if a reachability analysis had never been performed from this state; false otherwise.
     */
    public boolean performReachabilityFrom(State si){



        HashableState sih = this.stateHash(si);
        //if this is not a new state and we are not required to perform a new reachability analysis, then this method does not need to do anything.
        if(valueFunction.containsKey(sih) && this.foundReachableStates){
            return false; //no need for additional reachability testing
        }

        //add to the open list
        LinkedList <HashableState> openList = new LinkedList<HashableState>();
        Set <HashableState> openedSet = new HashSet<HashableState>();
        openList.offer(sih);
        openedSet.add(sih);


        while(!openList.isEmpty()){
            HashableState sh = openList.poll();

            //skip this if it's already been expanded
            if(valueFunction.containsKey(sh)){
                continue;
            }

            //do not need to expand from terminal states if set to prune
            if(this.model.terminal(sh.s()) && stopReachabilityFromTerminalStates){
                continue;
            }

            this.valueFunction.put(sh, this.valueInitializer.value(sh.s()));


            List<Action> actions = this.applicableActions(sh.s());
            for(Action a : actions){
                List<TransitionProb> tps = ((FullModel)model).transitions(sh.s(), a);
                for(TransitionProb tp : tps){
                    HashableState tsh = this.stateHash(tp.eo.op);
                    boolean inOpenSet = openedSet.contains(tsh);
                    if (!inOpenSet) {
                        boolean alreadyInVF = valueFunction.containsKey(tsh);
                        if (!alreadyInVF) {
                            openedSet.add(tsh);
                            openList.offer(tsh);
                        }
                    }
                }
            }


        }

        this.foundReachableStates = true;
        this.hasRunVI = false;

        return true;

    }

    public ValueFunction saveValueFunction(double defaultValue, double cutoffValue) {
        if (!this.hasRunVI) {
            throw new RuntimeException("ERROR: have not run value iteration yet");
        }
        ValueFunction valueFunction = new TabularValueFunction(hashingFactory, this.valueFunction, defaultValue);
        for(Iterator<Map.Entry<HashableState, Double>> it = this.valueFunction.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<HashableState, Double> entry = it.next();
            if(entry.getValue() >= cutoffValue) {
                it.remove();
            }
        }
        return valueFunction;
    }

    public Map<HashableState,Double> getValueFunction() {
        return valueFunction;
    }
}
