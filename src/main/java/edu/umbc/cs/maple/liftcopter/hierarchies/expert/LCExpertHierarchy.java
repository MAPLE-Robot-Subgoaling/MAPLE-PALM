//package edu.umbc.cs.maple.liftCopter.hierarchies.expert;
//
//import burlap.mdp.core.action.ActionType;
//import burlap.mdp.singleagent.oo.OOSADomain;
//import edu.umbc.cs.maple.config.ExperimentConfig;
//import edu.umbc.cs.maple.hierarchy.framework.*;
//import edu.umbc.cs.maple.liftcopter.LiftCopter;
//import edu.umbc.cs.maple.liftcopter.hierarchies.LCHierarchy;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.LCGetDomain;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.state.GetStateMapper;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.nav.LCNavDomain;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.nav.state.NavStateMapper;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.put.LCPutDomain;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.put.state.PutStateMapper;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.root.LCRootDomain;
//import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.root.state.RootStateMapper;
//import edu.umbc.cs.maple.liftcopter.hierarchies.functions.*;
//import org.yaml.snakeyaml.Yaml;
//
//import java.util.List;
//
//import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;
//
//public class LCExpertHierarchy extends LCHierarchy {
//
//    /***
//     * creates the standards copter hierarchy and returns the root task
//     * @param correctMoveprob the transitionProbability that a movement action will work as expected
//     * @return the root task of the copter hierarchy
//     */
//    @Override
//    public Task createHierarchy(double correctMoveprob, boolean plan){
//        // Setup copter domain
//        LiftCopter copterDomain;
//
//            copterDomain = new LiftCopter(correctMoveprob);
//
//        LCNavDomain navDomainGen = new LCNavDomain();
//        // Domains
//        OOSADomain rootDomain = (new LCRootDomain()).generateDomain();
//        OOSADomain getDomain = (new LCGetDomain()).generateDomain();
//        OOSADomain putDomain = (new LCPutDomain()).generateDomain();
//        OOSADomain navDomain = navDomainGen.generateDomain();
//        baseDomain = copterDomain.generateDomain();
//        rootDomain.setModel(null);
//        getDomain.setModel(null);
//        putDomain.setModel(null);
//        navDomain.setModel(null);
//
//        // Navigate Tasks (Primitives used for Put Nav and Get Nav later)
//        ActionType aIdle = navDomain.getAction(ACTION_IDLE);
//        PrimitiveTask idle = new PrimitiveTask(aIdle, baseDomain);
//        //thrust_0.02_1.5
//        List<Double> thrustValues = navDomainGen.getThrustValues();
//        List<Double> directionValues = navDomainGen.getDirectionValues();
//        int numNavPrimitives = 1+thrustValues.size()*directionValues.size();
//        Task[] navTasks = new Task[numNavPrimitives];
//        navTasks[0] = idle;
//        int i = 1;
//        for (Double thrustVal: thrustValues) {
//            for (Double dirVal: directionValues) {
//                ActionType primActType = navDomain.getAction("thrust_"+thrustVal+"_"+dirVal);
//                PrimitiveTask primTask = new PrimitiveTask(primActType, baseDomain);
//                navTasks[i] = primTask;
//                i++;
//            }
//        }
//
//        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
//        double noopReward = NonprimitiveTask.NOOP_REWARD;
//
//        // Nav (Task used by Get and Put)
//        ActionType aNavigate = putDomain.getAction(ACTION_NAV);
//        GoalFailTF navTF = new GoalFailTF(new NavFailurePF(), null, new NavCompletedPF(), null);
//        GoalFailRF navRF = new GoalFailRF(navTF, defaultReward, noopReward);
//        NonprimitiveTask navigate = new NonprimitiveTask(
//                navTasks,
//                aNavigate,
//                navDomain,
//                new NavStateMapper(),
//                navTF,
//                navRF
//        );
//        if (plan) { setupKnownTFRF(navigate); }
//
//        // Pickup
//        ActionType aPickup = getDomain.getAction(ACTION_PICKUP);
//        PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
//
//        // Get (Task used by Root)
//        ActionType aGet = rootDomain.getAction(ACTION_GET);
//        Task[] getTasks = {pickup, navigate};
//        GoalFailTF getTF = new GoalFailTF(new GetFailurePF(), null, new GetCompletedPF(), null);
//        GoalFailRF getRF = new GoalFailRF(getTF, defaultReward, noopReward);
//        NonprimitiveTask get = new NonprimitiveTask(
//                getTasks,
//                aGet,
//                getDomain,
//                new GetStateMapper(),
//                getTF,
//                getRF
//        );
//        if (plan) { setupKnownTFRF(get); }
//
//        // Putdown
//        ActionType aPutdown = putDomain.getAction(ACTION_PUTDOWN);
//        PrimitiveTask putdown = new PrimitiveTask(aPutdown, baseDomain);
//
//        // Put (Task used by Root)
//        ActionType aPut = rootDomain.getAction(ACTION_PUT);
//        Task[] putTasks = {putdown, navigate};
//        GoalFailTF putTF = new GoalFailTF(new PutFailurePF(), null, new PutCompletedPF(), null);
//        GoalFailRF putRF = new GoalFailRF(putTF, defaultReward, noopReward);
//        NonprimitiveTask put = new NonprimitiveTask(
//                putTasks,
//                aPut,
//                putDomain,
//                new PutStateMapper(),
//                putTF,
//                putRF
//        );
//        if (plan) { setupKnownTFRF(put); }
//
//        // Root
//        ActionType aSolve = new SolveActionType();
//        Task[] rootTasks = {get, put};
//        GoalFailTF rootTF = new GoalFailTF(new RootFailurePF(), null, new RootCompletedPF(), null);
//        GoalFailRF rootRF = new GoalFailRF(rootTF, defaultReward, noopReward);
//        NonprimitiveTask root = new NonprimitiveTask(
//                rootTasks,
//                aSolve,
//                rootDomain,
//                new RootStateMapper(),
//                rootTF,
//                rootRF
//        );
//        if (plan) { setupKnownTFRF(root); }
//
//        return root;
//    }
//
//    public static void main( String[] args){
//        Yaml yaml = new Yaml();
//        System.out.println(yaml.dump(new LCExpertHierarchy().createHierarchy( 1.0, true)));
//    }
//
//    @Override
//    public Task createHierarchy(ExperimentConfig experimentConfig, boolean plan) {
//        return createHierarchy(1,true);
//    }
//}
