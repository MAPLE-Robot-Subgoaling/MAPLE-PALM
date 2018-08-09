//package edu.umbc.cs.maple.taxi.hierarchies;
//
//import burlap.mdp.auxiliary.StateMapping;
//import burlap.mdp.core.action.ActionType;
//import burlap.mdp.core.oo.propositional.PropositionalFunction;
//import edu.umbc.cs.maple.hierarchy.framework.*;
//import edu.umbc.cs.maple.taxi.PickupActionType;
//import edu.umbc.cs.maple.taxi.PutdownActionType;
//import edu.umbc.cs.maple.taxi.Taxi;
//import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask5ActionType;
//import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask7ActionType;
//import edu.umbc.cs.maple.taxi.hiergen.functions.FailureFunction;
//import edu.umbc.cs.maple.taxi.hiergen.functions.HierGenRootCompleted;
//import edu.umbc.cs.maple.taxi.hiergen.functions.HierGenTask5Completed;
//import edu.umbc.cs.maple.taxi.hiergen.functions.HierGenTask7Completed;
//import edu.umbc.cs.maple.taxi.hiergen.root.state.HierGenRootStateMapper;
//import edu.umbc.cs.maple.taxi.hiergen.task5.state.Task5StateMapper;
//import edu.umbc.cs.maple.taxi.hiergen.task7.state.Task7StateMapper;
//
//import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
//
//public class TaxiHierarchyHierGen extends TaxiHierarchy {
//
//    /***
//     * creates the hiergen taxi hierarchy and returns the root task
//     * @param correctMoveprob the transitionProbability that a movement action will work as expected
//     * @param fickleProbability the transitionProbability that a passenger in the taxi will change goals
//     * @return the root task of the taxi hierarchy
//     */
//    @Override
//    public Task createHierarchy(double correctMoveprob, double fickleProbability, boolean plan) {
//        Taxi taxiDomain;
//        if (fickleProbability == 0) {
//            taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
//        } else {
//            taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
//        }
//
//        baseDomain = taxiDomain.generateDomain();
//
//        ActionType aNorth = baseDomain.getAction(ACTION_NORTH);
//        ActionType aEast = baseDomain.getAction(ACTION_EAST);
//        ActionType aSouth = baseDomain.getAction(ACTION_SOUTH);
//        ActionType aWest = baseDomain.getAction(ACTION_WEST);
//        ActionType aPickup = new PickupActionType(ACTION_PICKUP, new String[]{CLASS_PASSENGER});//HGPickupActionType(ACTION_PICKUP, new String[]{TaxiHierGenTask7State.CLASS_TASK7_PASSENGER});
//        ActionType aPutdown = new PutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_PASSENGER});//HGPutdownActionType(ACTION_PUTDOWN, new String[]{TaxiHierGenRootState.CLASS_ROOT_PASSENGER});
//        ActionType aTask5 = new HierGenTask5ActionType();
//        ActionType aTask7 = new HierGenTask7ActionType(ACTION_TASK_7, new String[]{CLASS_PASSENGER});
//        ActionType asolve = new SolveActionType();
//
//        //state mapper
//        StateMapping task5Map = new Task5StateMapper();
//        StateMapping task7Map = new Task7StateMapper();
//        StateMapping rootMap = new HierGenRootStateMapper();
//
//        //tasks
//        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
//        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
//        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
//        PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
//        PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
//        PrimitiveTask dropoff = new PrimitiveTask(aPutdown, baseDomain);
//
//        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
//        double noopReward = NonprimitiveTask.NOOP_REWARD;
//
//        Task[] task5Children = {north, east, south, wast};
//        PropositionalFunction task5CompletedPF = new HierGenTask5Completed();
//        PropositionalFunction task5FailPF = new FailureFunction();
//        GoalFailTF t5TF = new GoalFailTF(new FailureFunction(), null, new HierGenTask5Completed(), null);
//        GoalFailRF t5RF = new GoalFailRF(t5TF, defaultReward, noopReward);
//        NonprimitiveTask task5 = new NonprimitiveTask(
//                task5Children,
//                aTask5,
//                baseDomain,
//                task5Map,
//                t5TF,
//                t5RF
//        );
//
//        Task[] task7Children = {task5, pickup};
//        GoalFailTF t7TF = new GoalFailTF(new FailureFunction(), null, new HierGenTask7Completed(), null);
//        GoalFailRF t7RF = new GoalFailRF(t7TF, defaultReward, noopReward);
//        NonprimitiveTask task7 = new NonprimitiveTask(
//                task7Children,
//                aTask7,
//                baseDomain,
//                task7Map,
//                t7TF,
//                t7RF
//        );
//
//        Task[] rootChildren = {task7, dropoff, task5};
//        GoalFailTF rootTF = new GoalFailTF(new FailureFunction(), null, new HierGenRootCompleted(), null);
//        GoalFailRF rootRF = new GoalFailRF(rootTF, defaultReward, noopReward);
//        NonprimitiveTask root = new NonprimitiveTask(
//                rootChildren,
//                asolve,
//                baseDomain,
//                rootMap,
//                rootTF,
//                rootRF
//        );
//
//        return root;
//    }
//
//}
