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

public class CleanupHierarchy {

    private static OOSADomain baseDomain;

    public static Task createAMDPHierarchy(int minX, int minY, int maxX, int maxY){
        Cleanup domainGenerator;
        domainGenerator = new Cleanup(minX, minY, maxX, maxY);

        DomainGenerator praDomainG = new Cleanup(minX, minY, maxX, maxY);
        DomainGenerator prbDomainG = new Cleanup(minX, minY, maxX, maxY);
        DomainGenerator madDomainG = new Cleanup(minX, minY, maxX, maxY);
        DomainGenerator marDomainG = new Cleanup(minX, minY, maxX, maxY);
        DomainGenerator mbdDomainG = new Cleanup(minX, minY, maxX, maxY);
        DomainGenerator mbrDomainG = new Cleanup(minX, minY, maxX, maxY);
        DomainGenerator rootDomainG = new Cleanup(minX, minY, maxX, maxY);

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
        PropositionalFunction agentDoorFail = new AgentDoorFailPF("AgentDoorFailPF", new String[]{});
        PropositionalFunction agentDoorGoal = new AgentDoorGoalPF("AgentDoorGoalPF", new String[]{});
        NonprimitiveTask agentToDoor = new NonprimitiveTask(subTasks, aMoveAgentToDoor, moveAgentDoorDomain,
                agentDoorMapper, agentDoorFail, agentDoorGoal);

        StateMapping agentRoomMapper = new AgentDoorMapper();
        PropositionalFunction agentRoomFail = new AgentDoorFailPF("AgentDoorFailPF", new String[]{});
        PropositionalFunction agentRoomGoal = new AgentDoorGoalPF("AgentDoorGoalPF", new String[]{});
        NonprimitiveTask agentToRoom = new NonprimitiveTask(subTasks, aMoveAgentToRoom, moveAgentRoomDomain,
                agentRoomMapper, agentRoomFail, agentRoomGoal);

        StateMapping blockDoorMapper = new AgentDoorMapper();
        PropositionalFunction blockDoorFail = new AgentDoorFailPF("AgentDoorFailPF", new String[]{});
        PropositionalFunction blockDoorGoal = new AgentDoorGoalPF("AgentDoorGoalPF", new String[]{});
        NonprimitiveTask blockToDoor = new NonprimitiveTask(subTasks, aMoveBlockToDoor, moveBlockDoorDomain,
                blockDoorMapper, blockDoorFail, blockDoorGoal);

        StateMapping blockRoomMapper = new AgentDoorMapper();
        PropositionalFunction blockRoomFail = new AgentDoorFailPF("AgentDoorFailPF", new String[]{});
        PropositionalFunction blockRoomGoal = new AgentDoorGoalPF("AgentDoorGoalPF", new String[]{});
        NonprimitiveTask blockToRoom = new NonprimitiveTask(subTasks, aMoveBlockToRoom, moveBlockRoomDomain,
                blockRoomMapper, blockRoomFail, blockRoomGoal);

        Task[] subTasks2 = {agentToDoor, agentToRoom, blockToDoor, blockToRoom};
        NonprimitiveTask pickRoomAgent = new NonprimitiveTask(subTasks2, aPickRoomForAgent, pickRoomAgentDomain,
                new PickRoomAgentMapper(), new PickRoomAgentFailPF("PickRoomAgentFailPF", new String[]{}), new PickRoomAgentGoalPF("PickRoomAgentGoalPF", new String[]{}));

        NonprimitiveTask pickRoomBlock = new NonprimitiveTask(subTasks2, aPickRoomForBlock, pickRoomBlockDomain,
                new PickRoomBlockMapper(), new PickRoomBlockFailPF("PickRoomBlockFailPF", new String[]{}), new PickRoomBlockGoalPF("PickRoomBlockGoalPF", new String[]{}));

        Task[] rootTasks = {pickRoomAgent, pickRoomBlock};
        NonprimitiveTask root = new NonprimitiveTask(rootTasks, aSolve, rootDomain,
                new CleanupRootMapper(), new CleanupRootPF(), new CleanupRootPF());

        return root;
    }


    public static OOSADomain getBaseDomain(){
        return baseDomain;
    }
}