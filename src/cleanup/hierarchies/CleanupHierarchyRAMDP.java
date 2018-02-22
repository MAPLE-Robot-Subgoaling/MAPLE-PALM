package cleanup.hierarchies;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import cleanup.Cleanup;
import cleanup.CleanupGoal;
import cleanup.CleanupGoalDescription;
import cleanup.hierarchies.tasks.move.*;
import cleanup.hierarchies.tasks.pick.*;
import cleanup.hierarchies.tasks.root.CleanupRoot;
import cleanup.hierarchies.tasks.root.CleanupRootFailPF;
import cleanup.hierarchies.tasks.root.CleanupRootGoalPF;
import cleanup.hierarchies.tasks.root.CleanupRootMapper;
import config.cleanup.CleanupConfig;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.PrimitiveTask;
import hierarchy.framework.SolveActionType;
import hierarchy.framework.Task;

import static cleanup.Cleanup.*;
import static cleanup.Cleanup.CLASS_BLOCK;
import static cleanup.Cleanup.CLASS_ROOM;

public class CleanupHierarchyRAMDP extends CleanupHierarchy {

    public CleanupHierarchyRAMDP() {

    }

    public Task createAMDPHierarchy(CleanupConfig config){

        int minX = config.minX;
        int minY = config.minY;
        int maxX = config.maxX;
        int maxY = config.maxY;
        double rewardGoal = config.rewardGoal; // 500;
        double rewardBase = config.rewardBase; // -1;
        double rewardNoop = config.rewardNoop; // -1;
        double rewardPull = config.rewardPull; // 0;

        CleanupGoal goalCondition = new CleanupGoal();

        RewardFunction rootRF = null;//new PickRF();//new CleanupRF(goalCondition, rewardGoal, rewardBase, rewardNoop, rewardPull);
        TerminalFunction rootTF = null;// new PickTF();//new GoalConditionTF(goalCondition);
        RewardFunction pickRF = null;// new PickRF();
        TerminalFunction pickTF = null;// new PickTF();
        RewardFunction moveRF = null;// new CleanupRF(goalCondition, rewardGoal, rewardBase, rewardNoop, rewardPull);;//new MoveRF();
        TerminalFunction moveTF = null;// new GoalConditionTF(goalCondition);//new MoveTF();

        Cleanup baseDomainG = new Cleanup(minX, minY, maxX, maxY);
        baseDomainG.setRf(moveRF);
        baseDomainG.setTf(moveTF);
        DomainGenerator madDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator marDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator mbdDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator mbrDomainG = new CleanupMove(minX, minY, maxX, maxY, moveRF, moveTF);
        DomainGenerator praDomainG = new CleanupPick(pickRF, pickTF);
        DomainGenerator prbDomainG = new CleanupPick(pickRF, pickTF);
        DomainGenerator rootDomainG = new CleanupRoot(rootRF, rootTF);

        //action type domain - not for tasks
        baseDomain = (OOSADomain) baseDomainG.generateDomain();
        OOSADomain moveAgentDoorDomain = (OOSADomain) madDomainG.generateDomain();
        OOSADomain moveAgentRoomDomain = (OOSADomain) marDomainG.generateDomain();
        OOSADomain moveBlockDoorDomain = (OOSADomain) mbdDomainG.generateDomain();
        OOSADomain moveBlockRoomDomain = (OOSADomain) mbrDomainG.generateDomain();
        OOSADomain pickRoomAgentDomain = (OOSADomain) praDomainG.generateDomain();
        OOSADomain pickRoomBlockDomain = (OOSADomain) prbDomainG.generateDomain();
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

        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
        double noopReward = NonprimitiveTask.NOOP_REWARD;

        StateMapping agentDoorMapper = new AgentDoorMapper();
        PropositionalFunction agentDoorFail = new ObjectInRegionFailPF("AgentDoorFailPF", new String[]{CLASS_AGENT, CLASS_DOOR});
        PropositionalFunction agentDoorGoal = new ObjectInRegionGoalPF("AgentDoorGoalPF", new String[]{CLASS_AGENT, CLASS_DOOR});
        NonprimitiveTask agentToDoor = new NonprimitiveTask(subTasks, aMoveAgentToDoor, moveAgentDoorDomain,
                agentDoorMapper, agentDoorFail, agentDoorGoal, defaultReward, noopReward);

        StateMapping agentRoomMapper = new AgentRoomMapper();
        PropositionalFunction agentRoomFail = new ObjectInRegionFailPF("AgentRoomFailPF", new String[]{CLASS_AGENT, CLASS_ROOM});
        PropositionalFunction agentRoomGoal = new ObjectInRegionGoalPF("AgentRoomGoalPF", new String[]{CLASS_AGENT, CLASS_ROOM});
        NonprimitiveTask agentToRoom = new NonprimitiveTask(subTasks, aMoveAgentToRoom, moveAgentRoomDomain,
                agentRoomMapper, agentRoomFail, agentRoomGoal, defaultReward, noopReward);

        StateMapping blockDoorMapper = new BlockDoorMapper();
        PropositionalFunction blockDoorFail = new ObjectInRegionFailPF("BlockDoorFailPF", new String[]{CLASS_BLOCK, CLASS_DOOR});
        PropositionalFunction blockDoorGoal = new ObjectInRegionGoalPF("BlockDoorGoalPF", new String[]{CLASS_BLOCK, CLASS_DOOR});
        NonprimitiveTask blockToDoor = new NonprimitiveTask(subTasks, aMoveBlockToDoor, moveBlockDoorDomain,
                blockDoorMapper, blockDoorFail, blockDoorGoal, defaultReward, noopReward);

        StateMapping blockRoomMapper = new BlockRoomMapper();
        PropositionalFunction blockRoomFail = new ObjectInRegionFailPF("BlockRoomFailPF", new String[]{CLASS_BLOCK, CLASS_ROOM});
        PropositionalFunction blockRoomGoal = new ObjectInRegionGoalPF("BlockRoomGoalPF", new String[]{CLASS_BLOCK, CLASS_ROOM});
        NonprimitiveTask blockToRoom = new NonprimitiveTask(subTasks, aMoveBlockToRoom, moveBlockRoomDomain,
                blockRoomMapper, blockRoomFail, blockRoomGoal, defaultReward, noopReward);

        Task[] subTasks2 = {agentToDoor, agentToRoom, blockToDoor, blockToRoom};
        NonprimitiveTask pickRoomAgent = new NonprimitiveTask(subTasks2, aPickRoomForAgent, pickRoomAgentDomain,
                new PickRoomAgentMapper(),
                new PickObjectRoomFailPF("PickAgentRoomFailPF", new String[]{CLASS_AGENT, CLASS_ROOM}),
                new PickObjectRoomGoalPF("PickAgentRoomGoalPF", new String[]{CLASS_AGENT, CLASS_ROOM}),
                defaultReward, noopReward);

        NonprimitiveTask pickRoomBlock = new NonprimitiveTask(subTasks2, aPickRoomForBlock, pickRoomBlockDomain,
                new PickRoomBlockMapper(),
                new PickObjectRoomFailPF("PickBlockRoomFailPF", new String[]{CLASS_BLOCK, CLASS_ROOM}),
                new PickObjectRoomGoalPF("PickBlockRoomGoalPF", new String[]{CLASS_BLOCK, CLASS_ROOM}),
                defaultReward, noopReward);

        Task[] rootTasks = {pickRoomAgent, pickRoomBlock};
//        System.err.println("DEBUG WARNING: removing pickRoomAgent and subtasks ENTIRELY");
//        Task[] rootTasks = {pickRoomBlock};

        NonprimitiveTask root = new NonprimitiveTask(rootTasks, aSolve, rootDomain,
                new CleanupRootMapper(),
                new CleanupRootFailPF("CleanupRootFailPF", new String[]{}),
                new CleanupRootGoalPF("CleanupRootGoalPF", goalCondition), defaultReward, noopReward);

        return root;
    }


}
