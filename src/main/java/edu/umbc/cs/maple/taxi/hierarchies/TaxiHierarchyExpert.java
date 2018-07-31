//package edu.umbc.cs.maple.taxi.hierarchies;
//
//import burlap.mdp.core.action.ActionType;
//import burlap.mdp.singleagent.oo.OOSADomain;
//import edu.umbc.cs.maple.hierarchy.framework.*;
//import edu.umbc.cs.maple.taxi.Taxi;
//import edu.umbc.cs.maple.taxi.functions.amdp.*;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.get.TaxiGetDomain;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.get.state.GetStateMapper;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.TaxiNavDomain;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.NavStateMapper;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.TaxiPutDomain;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.PutStateMapper;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.TaxiRootDomain;
//import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.RootStateMapper;
//
//import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
//
//public class TaxiHierarchyExpert extends TaxiHierarchy {
//
//
//    /***
//     * creates the standards taxi hierarchy and returns the root task
//     * @param correctMoveprob the transitionProbability that a movement action will work as expected
//     * @param fickleProbability the transitionProbability that a passenger in the taxi will change goals
//     * @return the root task of the taxi hierarchy
//     */
//    @Override
//    public Task createHierarchy(double correctMoveprob, double fickleProbability, boolean plan){
//        // Setup taxi domain
//        Taxi taxiDomain;
//        if(fickleProbability == 0){
//            taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
//        }else{
//            taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
//        }
//
//        // Domains
//        OOSADomain rootDomain = (new TaxiRootDomain()).generateDomain();
//        OOSADomain getDomain = (new TaxiGetDomain()).generateDomain();
//        OOSADomain putDomain = (new TaxiPutDomain()).generateDomain();
//        OOSADomain navDomain = (new TaxiNavDomain()).generateDomain();
//        baseDomain = taxiDomain.generateDomain();
//
//        // Navigate Tasks (Primitives used for Put Nav and Get Nav later)
//        ActionType aNorth = navDomain.getAction(ACTION_NORTH);
//        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
//        ActionType aEast = navDomain.getAction(ACTION_EAST);
//        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
//        ActionType aSouth = navDomain.getAction(ACTION_SOUTH);
//        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
//        ActionType aWest = navDomain.getAction(ACTION_WEST);
//        PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
//        Task[] navTasks = {north, east, south, west};
//
//        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
//        double noopReward = NonprimitiveTask.NOOP_REWARD;
//
//        // Nav (Task used by Get and Put)
//        ActionType aNavigate = putDomain.getAction(ACTION_NAV);
//        GoalFailTF navTF = new GoalFailTF(new NavFailurePF(), null,  new NavCompletedPF(), null);
//        GoalFailRF navRF = new GoalFailRF(navTF, defaultReward, noopReward);
//        NonprimitiveTask navigate = new NonprimitiveTask(
//                navTasks,
//                aNavigate,
//                navDomain,
//                new NavStateMapper(),
//                navTF,
//                navRF
//
//        );
//        if (plan) { setupKnownTFRF(navigate); } else { navDomain.setModel(null); }
//
//        // Pickup
//        ActionType aPickup = getDomain.getAction(ACTION_PICKUP);
//        PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
//
//        // Get (Task used by Root)
//        ActionType aGet = rootDomain.getAction(ACTION_GET);
//        Task[] getTasks = {pickup, navigate};
//        GoalFailTF getTF = new GoalFailTF(new GetFailurePF(), null,  new GetCompletedPF(), null);
//        GoalFailRF getRF = new GoalFailRF(getTF, defaultReward, noopReward);
//        NonprimitiveTask get = new NonprimitiveTask(
//                getTasks,
//                aGet,
//                getDomain,
//                new GetStateMapper(),
//                getTF,
//                getRF
//        );
//        if (plan) { setupKnownTFRF(get); } else { getDomain.setModel(null); }
//
//        // Putdown
//        ActionType aPutdown = putDomain.getAction(ACTION_PUTDOWN);
//        PrimitiveTask putdown = new PrimitiveTask(aPutdown, baseDomain);
//
//        // Put (Task used by Root)
//        ActionType aPut = rootDomain.getAction(ACTION_PUT);
//        Task[] putTasks = {putdown, navigate};
//        GoalFailTF putTF = new GoalFailTF(new PutFailurePF(), null,  new PutCompletedPF(), null);
//        GoalFailRF putRF = new GoalFailRF(putTF, defaultReward, noopReward);
//        NonprimitiveTask put = new NonprimitiveTask(
//                putTasks,
//                aPut,
//                putDomain,
//                new PutStateMapper(),
//                putTF,
//                putRF
//        );
//        if (plan) { setupKnownTFRF(put); } else { putDomain.setModel(null); }
//
//        // Root
//        ActionType aSolve = new SolveActionType();
//        Task[] rootTasks = {get, put};
//        GoalFailTF rootTF = new GoalFailTF(new RootFailurePF(), null,  new RootCompletedPF(), null);
//        GoalFailRF rootRF = new GoalFailRF(rootTF, defaultReward, noopReward);
//        NonprimitiveTask root = new NonprimitiveTask(
//                rootTasks,
//                aSolve,
//                rootDomain,
//                new RootStateMapper(),
//                rootTF,
//                rootRF
//        );
//        if (plan) { setupKnownTFRF(root); } else { rootDomain.setModel(null); }
//
//        return root;
//    }
//}
