//package edu.umbc.cs.maple.taxi.hierarchies;
//
//import burlap.mdp.core.action.ActionType;
//import burlap.mdp.core.oo.propositional.PropositionalFunction;
//import burlap.mdp.singleagent.oo.OOSADomain;
//import edu.umbc.cs.maple.hierarchy.framework.NonprimitiveTask;
//import edu.umbc.cs.maple.hierarchy.framework.PrimitiveTask;
//import edu.umbc.cs.maple.hierarchy.framework.SolveActionType;
//import edu.umbc.cs.maple.hierarchy.framework.Task;
//import edu.umbc.cs.maple.taxi.Taxi;
//import edu.umbc.cs.maple.taxi.functions.amdp.*;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.NavigateActionType;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.get.GetPickupActionType;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.get.state.GetStateMapper;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.NavStateMapper;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.PutPutdownActionType;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.PutStateMapper;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.GetActionType;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.PutActionType;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.RootStateMapper;
//import edu.umbc.cs.maple.taxi.rmaxq.functions.BaseRootPF;
//
//import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
//import static edu.umbc.cs.maple.taxi.TaxiConstants.CLASS_PASSENGER;
//
//public class TaxiHierarchyRMAXQ extends TaxiHierarchyExpert {
//
//
//
////    /**
////     * creates a taxi hierarchy with no abstractions
////     * @param correctMoveprob the transitionProbability that a movement action will work as expected
////     * @param fickleProbability the transitionProbability that a passenger in the taxi will change goals
////     * @return the root task of the taxi hierarchy
////     */
////    public static Task createRMAXQHierarchy(double correctMoveprob, double fickleProbability){
////        Taxi taxiDomain;
////
////        if(fickleProbability == 0){
////            taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
////        }else{
////            taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
////        }
////
////        //action type domain - not for tasks
////        baseDomain = taxiDomain.generateDomain();
////        OOSADomain getDomain = taxiDomain.generateDomain();
////        OOSADomain putDomain = taxiDomain.generateDomain();
////
////        ActionType aNorth = baseDomain.getAction(ACTION_NORTH);
////        ActionType aEast = baseDomain.getAction(ACTION_EAST);
////        ActionType aSouth = baseDomain.getAction(ACTION_SOUTH);
////        ActionType aWest = baseDomain.getAction(ACTION_WEST);
////        ActionType aPickup = new GetPickupActionType(ACTION_PICKUP, new String[]{CLASS_PASSENGER});
////        ActionType aPutdown = new PutPutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_PASSENGER});//putDomain.getAction(ACTION_PUTDOWN);
////        ActionType aNavigate = new NavigateActionType(ACTION_NAV, new String[]{CLASS_LOCATION});
////        ActionType aGet = new GetActionType(ACTION_GET, new String[]{CLASS_PASSENGER});
////        ActionType aPut = new PutActionType(ACTION_PUT, new String[]{CLASS_PASSENGER});
////        ActionType aSolve = new SolveActionType();
////
////        //tasks
////        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
////        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
////        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
////        PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
////        PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
////        PrimitiveTask putdown = new PrimitiveTask(aPutdown, baseDomain);
////
////        Task[] navTasks = new Task[]{north, east, south, wast};
////
////        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
////        double noopReward = NonprimitiveTask.NOOP_REWARD;
////
////        PropositionalFunction navFailPF = new NavFailurePF();
////        PropositionalFunction navCompPF = new NavCompletedPF();
////        OOSADomain navDomain = taxiDomain.generateNavigateDomain();
////        navDomain.setModel(null);
////        NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, navDomain,
////                new NavStateMapper(), navFailPF, navCompPF, defaultReward, noopReward);
////
////        Task[] getTasks = new Task[]{pickup, navigate};
////        Task[] putTasks = new Task[]{navigate, putdown};
////
////        PropositionalFunction getFailPF = new GetFailurePF();
////        PropositionalFunction getCompPF = new GetCompletedPF();
////        getDomain.setModel(null);
////        NonprimitiveTask get = new NonprimitiveTask(
////                getTasks,
////                aGet,
////                getDomain,
////                new GetStateMapper(),
////                getFailPF,//new GetFailurePF(),
////                getCompPF,//new GetCompletedPF(),
////                defaultReward,
////                noopReward
////        );
////
////        PropositionalFunction putFailPF = new PutFailurePF();
////        PropositionalFunction putCompPF = new PutCompletedPF();
////        putDomain.setModel(null);
////        NonprimitiveTask put = new NonprimitiveTask(
////                putTasks,
////                aPut,
////                putDomain, new PutStateMapper(),
////                putFailPF,//new PutFailurePF(),
////                putCompPF,//new PutCompletedPF(),
////                defaultReward,
////                noopReward
////        );
////
////        Task[] rootTasks = {get, put};
////        PropositionalFunction rootPF = new BaseRootPF();
////        OOSADomain rootDomain = taxiDomain.generateDomain();
////        rootDomain.setModel(null);
////        Task root = new NonprimitiveTask(rootTasks, aSolve, rootDomain,
////                new RootStateMapper(),
////                new RootFailurePF(),
////                new RootCompletedPF(),
////                defaultReward,
////                noopReward
////        );
////
////        return root;
//////		throw new RuntimeException("need to be reimplemented");
////
////    }
//
//}
