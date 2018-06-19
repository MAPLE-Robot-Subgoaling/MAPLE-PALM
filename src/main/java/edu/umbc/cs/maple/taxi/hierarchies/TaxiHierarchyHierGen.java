package edu.umbc.cs.maple.taxi.hierarchies;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.NonprimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.PrimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.SolveActionType;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.taxi.PickupActionType;
import edu.umbc.cs.maple.taxi.PutdownActionType;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.hiergen.task5.state.Task5StateMapper;
import edu.umbc.cs.maple.taxi.hiergen.task7.state.Task7StateMapper;
import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask5ActionType;
import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask7ActionType;
import edu.umbc.cs.maple.taxi.hiergen.functions.FailureFunction;
import edu.umbc.cs.maple.taxi.hiergen.functions.HierGenRootCompleted;
import edu.umbc.cs.maple.taxi.hiergen.functions.HierGenTask5Completed;
import edu.umbc.cs.maple.taxi.hiergen.functions.HierGenTask7Completed;
import edu.umbc.cs.maple.taxi.hiergen.root.state.HierGenRootStateMapper;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
import static edu.umbc.cs.maple.taxi.TaxiConstants.ACTION_PICKUP;
import static edu.umbc.cs.maple.taxi.TaxiConstants.ACTION_PUTDOWN;

public class TaxiHierarchyHierGen extends TaxiHierarchy {

    /***
     * creates the hiergen taxi hierarchy and returns the root task
     * @param correctMoveprob the transitionProbability that a movement action will work as expected
     * @param fickleProbability the transitionProbability that a passenger in the taxi will change goals
     * @return the root task of the taxi hierarchy
     */
    @Override
    public Task createHierarchy(double correctMoveprob, double fickleProbability, boolean plan) {
        Taxi taxiDomain;
        if (fickleProbability == 0) {
            taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
        } else {
            taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
        }

        baseDomain = taxiDomain.generateDomain();

        ActionType aNorth = baseDomain.getAction(ACTION_NORTH);
        ActionType aEast = baseDomain.getAction(ACTION_EAST);
        ActionType aSouth = baseDomain.getAction(ACTION_SOUTH);
        ActionType aWest = baseDomain.getAction(ACTION_WEST);
        ActionType aPickup = new PickupActionType(ACTION_PICKUP, new String[]{CLASS_PASSENGER});//HGPickupActionType(ACTION_PICKUP, new String[]{TaxiHierGenTask7State.CLASS_TASK7_PASSENGER});
        ActionType aPutdown = new PutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_PASSENGER});//HGPutdownActionType(ACTION_PUTDOWN, new String[]{TaxiHierGenRootState.CLASS_ROOT_PASSENGER});
        ActionType aTask5 = new HierGenTask5ActionType();
        ActionType aTask7 = new HierGenTask7ActionType(ACTION_TASK_7, new String[]{CLASS_PASSENGER});
        ActionType asolve = new SolveActionType();

        //state mapper
        StateMapping task5Map = new Task5StateMapper();
        StateMapping task7Map = new Task7StateMapper();
        StateMapping rootMap = new HierGenRootStateMapper();

        //tasks
        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
        PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
        PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
        PrimitiveTask dropoff = new PrimitiveTask(aPutdown, baseDomain);

        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
        double noopReward = NonprimitiveTask.NOOP_REWARD;

        Task[] task5Children = {north, east, south, wast};
        PropositionalFunction task5CompletedPF = new HierGenTask5Completed();
        PropositionalFunction task5FailPF = new FailureFunction();
        NonprimitiveTask task5 = new NonprimitiveTask(
                task5Children,
                aTask5,
                baseDomain,
                task5Map,
                task5FailPF,
                task5CompletedPF,
                defaultReward,
                noopReward
        );

        Task[] task7Children = {task5, pickup};
        PropositionalFunction task7CompletedPF = new HierGenTask7Completed();
        PropositionalFunction task7FailPF = new FailureFunction();
        NonprimitiveTask task7 = new NonprimitiveTask(
                task7Children,
                aTask7,
                baseDomain,
                task7Map,
                task7FailPF,
                task7CompletedPF,
                defaultReward,
                noopReward
        );

        Task[] rootChildren = {task7, dropoff, task5};
        PropositionalFunction rootCompletedPF = new HierGenRootCompleted();
        PropositionalFunction rootFailPF = new FailureFunction();
        NonprimitiveTask root = new NonprimitiveTask(
                rootChildren,
                asolve,
                baseDomain,
                rootMap,
                rootFailPF,
                rootCompletedPF,
                defaultReward,
                noopReward
        );

        return root;
    }

}
