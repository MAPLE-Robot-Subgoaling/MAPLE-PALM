package cleanup.hierarchies;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import cleanup.Cleanup;
import cleanup.CleanupGoal;
import cleanup.CleanupGoalDescription;
import cleanup.CleanupRF;
import cleanup.hierarchies.tasks.move.*;
import cleanup.hierarchies.tasks.pick.*;
import cleanup.hierarchies.tasks.root.CleanupRoot;
import cleanup.hierarchies.tasks.root.CleanupRootGoalPF;
import cleanup.hierarchies.tasks.root.CleanupRootMapper;
import cleanup.hierarchies.tasks.root.CleanupRootFailPF;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.PrimitiveTask;
import hierarchy.framework.SolveActionType;
import hierarchy.framework.Task;

import static cleanup.Cleanup.*;

public class CleanupHierarchy {

    private static OOSADomain baseDomain;

    public static Task createAMDPHierarchy(int minX, int minY, int maxX, int maxY){

        CleanupGoal goalCondition = new CleanupGoal();

        double rewardGoal = 1000;
        double rewardBase = 0;
        double rewardNoop = -1;
        double rewardPull = 0;
        RewardFunction rootRF = new PickRF();//new CleanupRF(goalCondition, rewardGoal, rewardBase, rewardNoop, rewardPull);
        TerminalFunction rootTF = new PickTF();//new GoalConditionTF(goalCondition);
        RewardFunction pickRF = new PickRF();
        TerminalFunction pickTF = new PickTF();
        RewardFunction moveRF = new MoveRF();
        TerminalFunction moveTF = new MoveTF();

        DomainGenerator baseDomainG = new Cleanup(minX, minY, maxX, maxY);
        DomainGenerator madDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator marDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator mbdDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator mbrDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator praDomainG = new CleanupPick(pickRF, pickTF);
        DomainGenerator prbDomainG = new CleanupPick(pickRF, pickTF);
        DomainGenerator rootDomainG = new CleanupRoot(rootRF, rootTF);

                //action type domain - not for tasks
        baseDomain = (OOSADomain) baseDomainG.generateDomain();
        OOSADomain pickRoomAgentDomain = (OOSADomain) praDomainG.generateDomain();
        OOSADomain pickRoomBlockDomain = (OOSADomain) prbDomainG.generateDomain();
        OOSADomain moveAgentDoorDomain = (OOSADomain) madDomainG.generateDomain();
        OOSADomain moveAgentRoomDomain = (OOSADomain) marDomainG.generateDomain();
        OOSADomain moveBlockDoorDomain = (OOSADomain) mbdDomainG.generateDomain();
        OOSADomain moveBlockRoomDomain = (OOSADomain) mbrDomainG.generateDomain();
        OOSADomain rootDomain          = (OOSADomain) rootDomainG.generateDomain();

        CleanupGoalDescription[] goals = new CleanupGoalDescription[]{
                new CleanupGoalDescription(new String[]{"block0", "room1"}, baseDomain.propFunction(PF_BLOCK_IN_ROOM)),
        };
        goalCondition.setGoals(goals);

        ActionType aNorth = baseDomain.getAction(Cleanup.ACTION_NORTH);
        ActionType aEast = baseDomain.getAction(Cleanup.ACTION_EAST);
        ActionType aSouth = baseDomain.getAction(Cleanup.ACTION_SOUTH);
        ActionType aWest = baseDomain.getAction(Cleanup.ACTION_WEST);
        ActionType aPull = baseDomain.getAction(Cleanup.ACTION_PULL);

        ActionType aMoveAgentToDoor = pickRoomAgentDomain.getAction(CleanupPick.ACTION_MOVE_AGENT_DOOR);
        ActionType aMoveAgentToRoom = pickRoomAgentDomain.getAction(CleanupPick.ACTION_MOVE_AGENT_ROOM);
        ActionType aMoveBlockToDoor = pickRoomBlockDomain.getAction(CleanupPick.ACTION_MOVE_BLOCK_DOOR);
        ActionType aMoveBlockToRoom = pickRoomBlockDomain.getAction(CleanupPick.ACTION_MOVE_BLOCK_ROOM);

        ActionType aPickRoomForAgent = rootDomain.getAction(CleanupRoot.ACTION_PICK_ROOM_AGENT);
        ActionType aPickRoomForBlock = rootDomain.getAction(CleanupRoot.ACTION_PICK_ROOM_BLOCK);
        ActionType aSolve = new SolveActionType();

        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
        PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
        PrimitiveTask pull = new PrimitiveTask(aPull, baseDomain);

        Task[] subTasks = {north, south, east, west, pull};

        StateMapping agentDoorMapper = new AgentDoorMapper();
        PropositionalFunction agentDoorFail = new ObjectInRegionFailPF("AgentDoorFailPF", new String[]{CLASS_AGENT, CLASS_DOOR});
        PropositionalFunction agentDoorGoal = new ObjectInRegionGoalPF("AgentDoorGoalPF", new String[]{CLASS_AGENT, CLASS_DOOR});
        NonprimitiveTask agentToDoor = new NonprimitiveTask(subTasks, aMoveAgentToDoor, moveAgentDoorDomain,
                agentDoorMapper, agentDoorFail, agentDoorGoal);

        StateMapping agentRoomMapper = new AgentDoorMapper();
        PropositionalFunction agentRoomFail = new ObjectInRegionFailPF("AgentRoomFailPF", new String[]{CLASS_AGENT, CLASS_ROOM});
        PropositionalFunction agentRoomGoal = new ObjectInRegionGoalPF("AgentRoomGoalPF", new String[]{CLASS_AGENT, CLASS_ROOM});
        NonprimitiveTask agentToRoom = new NonprimitiveTask(subTasks, aMoveAgentToRoom, moveAgentRoomDomain,
                agentRoomMapper, agentRoomFail, agentRoomGoal);

        StateMapping blockDoorMapper = new AgentDoorMapper();
        PropositionalFunction blockDoorFail = new ObjectInRegionFailPF("BlockDoorFailPF", new String[]{CLASS_BLOCK, CLASS_DOOR});
        PropositionalFunction blockDoorGoal = new ObjectInRegionGoalPF("BlockDoorGoalPF", new String[]{CLASS_BLOCK, CLASS_DOOR});
        NonprimitiveTask blockToDoor = new NonprimitiveTask(subTasks, aMoveBlockToDoor, moveBlockDoorDomain,
                blockDoorMapper, blockDoorFail, blockDoorGoal);

        StateMapping blockRoomMapper = new AgentDoorMapper();
        PropositionalFunction blockRoomFail = new ObjectInRegionFailPF("BlockRoomFailPF", new String[]{CLASS_BLOCK, CLASS_ROOM});
        PropositionalFunction blockRoomGoal = new ObjectInRegionGoalPF("BlockRoomGoalPF", new String[]{CLASS_BLOCK, CLASS_ROOM});
        NonprimitiveTask blockToRoom = new NonprimitiveTask(subTasks, aMoveBlockToRoom, moveBlockRoomDomain,
                blockRoomMapper, blockRoomFail, blockRoomGoal);

        Task[] subTasks2 = {agentToDoor, agentToRoom, blockToDoor, blockToRoom};
        NonprimitiveTask pickRoomAgent = new NonprimitiveTask(subTasks2, aPickRoomForAgent, pickRoomAgentDomain,
                new PickRoomAgentMapper(),
                new PickObjectRoomFailPF("PickAgentRoomFailPF", new String[]{CLASS_AGENT, CLASS_ROOM}),
                new PickObjectRoomGoalPF("PickAgentRoomGoalPF", new String[]{CLASS_AGENT, CLASS_ROOM}));

        NonprimitiveTask pickRoomBlock = new NonprimitiveTask(subTasks2, aPickRoomForBlock, pickRoomBlockDomain,
                new PickRoomBlockMapper(),
                new PickObjectRoomFailPF("PickBlockRoomFailPF", new String[]{CLASS_BLOCK, CLASS_ROOM}),
                new PickObjectRoomGoalPF("PickBlockRoomGoalPF", new String[]{CLASS_BLOCK, CLASS_ROOM}));

        Task[] rootTasks = {pickRoomAgent, pickRoomBlock};
        NonprimitiveTask root = new NonprimitiveTask(rootTasks, aSolve, rootDomain,
                new CleanupRootMapper(),
                new CleanupRootFailPF("CleanupRootFailPF", new String[]{}),
                new CleanupRootGoalPF("CleanupRootGoalPF", goalCondition));

        return root;
    }


    public static OOSADomain getBaseDomain(){
        return baseDomain;
    }
}