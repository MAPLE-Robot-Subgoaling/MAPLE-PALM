package cleanup.hierarchies;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import cleanup.Cleanup;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.PrimitiveTask;
import hierarchy.framework.SolveActionType;
import hierarchy.framework.Task;
import taxi.functions.amdp.RootPF;
import taxi.hierarchies.tasks.root.state.RootStateMapper;

public class CleanupHierarchy {

    private static OOSADomain baseDomain;

    public static Task createAMDPHierarchy(int minX, int minY, int maxX, int maxY){
        Cleanup domainGenerator;
        domainGenerator = new Cleanup(minX, minY, maxX, maxY);

        DomainGenerator praDomainG = null;
        DomainGenerator prbDomainG = null;
        DomainGenerator madDomainG = null;
        DomainGenerator marDomainG = null;
        DomainGenerator mbdDomainG = null;
        DomainGenerator mbrDomainG = null;
        DomainGenerator rootDomainG = null;

        //action type domain - not for tasks
        baseDomain = (OOSADomain) domainGenerator.generateDomain();
        OOSADomain pickRoomAgentDomain = (OOSADomain) praDomainG.generateDomain();
        OOSADomain pickRoomBlockDomain = (OOSADomain) prbDomainG.generateDomain();
        OOSADomain moveAgentDoorDomain = (OOSADomain) madDomainG.generateDomain();
        OOSADomain moveAgentRoomDomain = (OOSADomain) marDomainG.generateDomain();
        OOSADomain moveBlockDoorDomain = (OOSADomain) mbdDomainG.generateDomain();
        OOSADomain moveBlockRoomDomain = (OOSADomain) mbrDomainG.generateDomain();
        OOSADomain rootDomain          = (OOSADomain) rootDomainG.generateDomain();

        ActionType aNorth = baseDomain.getAction(Cleanup.ACTION_NORTH);
        ActionType aEast = baseDomain.getAction(Cleanup.ACTION_EAST);
        ActionType aSouth = baseDomain.getAction(Cleanup.ACTION_SOUTH);
        ActionType aWest = baseDomain.getAction(Cleanup.ACTION_WEST);
        ActionType aPull = baseDomain.getAction(Cleanup.ACTION_PULL);
        ActionType aPickRoomForAgent = new PickRoomForObjectActionType(
                "pickRoomForAgent", new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_ROOM});
        ActionType aPickRoomForBlock = new PickRoomForObjectActionType(
                "pickRoomForBlock", new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_ROOM});
        ActionType aMoveAgentToDoor = new ObjectToRegionActionType(
                "moveAgentToDoor", new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_DOOR});
        ActionType aMoveAgentToRoom = new ObjectToRegionActionType(
                "moveAgentToRoom", new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_ROOM});
        ActionType aMoveBlockToDoor = new ObjectToRegionActionType(
                "moveBlockToDoor", new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_DOOR});
        ActionType aMoveBlockToRoom = new ObjectToRegionActionType(
                "moveBlockToRoom", new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_ROOM});
        ActionType aSolve = new SolveActionType();

        //tasks
        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
        PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
        PrimitiveTask pull = new PrimitiveTask(aPull, baseDomain);

        Task[] subTasks = {north, south, east, west, pull};

        StateMapping agentDoorMapper = new AgentDoorMapper();
        PropositionalFunction agentDoorFail = new AgentDoorFailPF();
        PropositionalFunction agentDoorGoal = new AgentDoorGoalPF();
        NonprimitiveTask agentToDoor = new NonprimitiveTask(subTasks, aMoveAgentToDoor, moveAgentDoorDomain,
                agentDoorMapper, agentDoorFail, agentDoorGoal);

        StateMapping agentRoomMapper = new AgentDoorMapper();
        PropositionalFunction agentRoomFail = new AgentDoorFailPF();
        PropositionalFunction agentRoomGoal = new AgentDoorGoalPF();
        NonprimitiveTask agentToRoom = new NonprimitiveTask(subTasks, aMoveAgentToRoom, moveAgentRoomDomain,
                agentRoomMapper, agentRoomFail, agentRoomGoal);

        StateMapping blockDoorMapper = new AgentDoorMapper();
        PropositionalFunction blockDoorFail = new AgentDoorFailPF();
        PropositionalFunction blockDoorGoal = new AgentDoorGoalPF();
        NonprimitiveTask blockToDoor = new NonprimitiveTask(subTasks, aMoveBlockToDoor, moveBlockDoorDomain,
                blockDoorMapper, blockDoorFail, blockDoorGoal);

        StateMapping blockRoomMapper = new AgentDoorMapper();
        PropositionalFunction blockRoomFail = new AgentDoorFailPF();
        PropositionalFunction blockRoomGoal = new AgentDoorGoalPF();
        NonprimitiveTask blockToRoom = new NonprimitiveTask(subTasks, aMoveBlockToRoom, moveBlockRoomDomain,
                blockRoomMapper, blockRoomFail, blockRoomGoal);

        Task[] subTasks2 = {agentToDoor, agentToRoom, blockToDoor, blockToRoom};
        NonprimitiveTask pickRoomAgent = new NonprimitiveTask(, aPickRoomForAgent, pickRoomAgentDomain,
                new PickRoomAgentMapper(), new PickRoomAgentFailPF(), new PickRoomAgentGoalPF());

        NonprimitiveTask pickRoomBlock = new NonprimitiveTask(, aPickRoomForBlock, pickRoomBlockDomain,
                new PickRoomBlockMapper(), new PickRoomBlockFailPF(), new PickRoomBlockGoalPF());

        Task[] rootTasks = {pickRoomAgent, pickRoomBlock};
        NonprimitiveTask root = new NonprimitiveTask(rootTasks, aSolve, rootDomain,
                new RootStateMapper(), new RootPF(), new RootPF());

        return root;
    }

//    public static Task createRMAXQHierarchy(double correctMoveprob, double fickleProbability){
//        Taxi taxiDomain;
//
//        if(fickleProbability == 0){
//            taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
//        }else{
//            taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
//        }
//
//        //action type domain - not for tasks
//        baseDomain = taxiDomain.generateDomain();
//
//        ActionType aNorth = baseDomain.getAction(Taxi.ACTION_NORTH);
//        ActionType aEast = baseDomain.getAction(Taxi.ACTION_EAST);
//        ActionType aSouth = baseDomain.getAction(Taxi.ACTION_SOUTH);
//        ActionType aWest = baseDomain.getAction(Taxi.ACTION_WEST);
//        ActionType aPickup = baseDomain.getAction(Taxi.ACTION_PICKUP);
//        ActionType aPutdown = baseDomain.getAction(Taxi.ACTION_PUTDOWN);
//        ActionType aNavigate = new BaseNavigateActionType();
//        ActionType aGet = new BaseGetActionType();
//        ActionType aPut = new BasePutActionType();
//
//        //tasks
//        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
//        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
//        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
//        PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
//        PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
//        PrimitiveTask dropoff = new PrimitiveTask(aPutdown, baseDomain);
//
//        Task[] navTasks = new Task[]{north, east, south, wast};
//        Task[] bringonTasks = new Task[]{pickup};
//        Task[] dropoffTasks = new Task[]{dropoff};
//
//        PropositionalFunction navPF =/* new NavigateAbstractPF()*/ new NavigatePF();
//        NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, taxiDomain.generateNavigateDomain(),
//                new IdentityMap(), navPF, navPF);
//
//        PropositionalFunction pickupFailPF = new BringonFailurePF();
//        PropositionalFunction pickupCompPF = new BringonCompletedPF();
//        NonprimitiveTask pickupL1 = new NonprimitiveTask(bringonTasks, aPickup, pickupFailPF, pickupCompPF);
//
//        PropositionalFunction dropoffFailPF = new DropoffFailurePF();
//        PropositionalFunction dropoffCompPF = new DropoffCompletedPF();
//        NonprimitiveTask dropoffL1 = new NonprimitiveTask(dropoffTasks, aPutdown, dropoffFailPF, dropoffCompPF);
//
//        Task[] getTasks = new Task[]{pickupL1, navigate};
//        Task[] putTasks = new Task[]{navigate, dropoffL1};
//
//        PropositionalFunction getFailPF = new BaseGetFailurePF();
//        PropositionalFunction getCompPF = new BaseGetCompletedPF();
//        NonprimitiveTask get = new NonprimitiveTask(getTasks, aGet, getFailPF, getCompPF);
//
//        PropositionalFunction putFailPF = new BasePutFailurePF();
//        PropositionalFunction putCompPF = new BasePutCompletedPF();
//        NonprimitiveTask put = new NonprimitiveTask(putTasks, aPut, putFailPF, putCompPF);
//
//        Task[] rootTasks = {get, put};
//        Task root = new RootTask(rootTasks, baseDomain, new IdentityMap());
//
//        return root;
//
//    }

    public static OOSADomain getBaseDomain(){
        return baseDomain;
    }
}