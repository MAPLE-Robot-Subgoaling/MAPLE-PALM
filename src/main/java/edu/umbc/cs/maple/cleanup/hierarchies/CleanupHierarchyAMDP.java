package edu.umbc.cs.maple.cleanup.hierarchies;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.CleanupGoal;
import edu.umbc.cs.maple.cleanup.CleanupGoalDescription;
import edu.umbc.cs.maple.cleanup.CleanupRF;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.move.*;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.*;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.root.CleanupRoot;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.root.CleanupRootFailPF;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.root.CleanupRootGoalPF;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.root.CleanupRootMapper;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.config.cleanup.CleanupConfig;
import edu.umbc.cs.maple.hierarchy.framework.NonprimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.PrimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.SolveActionType;
import edu.umbc.cs.maple.hierarchy.framework.Task;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class CleanupHierarchyAMDP extends CleanupHierarchy {

    public CleanupHierarchyAMDP() {

    }

    @Override
    public Task createHierarchy(ExperimentConfig experimentConfig, boolean plan){

        CleanupConfig config = (CleanupConfig) experimentConfig.domain;
        int minX = config.minX;
        int minY = config.minY;
        int maxX = config.maxX;
        int maxY = config.maxY;
        double rewardGoal = config.rewardGoal; // 500;
        double rewardBase = config.rewardBase; // -1;
        double rewardNoop = config.rewardNoop; // -1;
        double rewardPull = config.rewardPull; // 0;

        CleanupGoal goalCondition = new CleanupGoal();

        RewardFunction rootRF   = null;// new CleanupRF(goalCondition, rewardGoal, rewardBase, rewardNoop, rewardPull);
        TerminalFunction rootTF = null;// new GoalConditionTF(goalCondition);
        RewardFunction pickRF   = null;// new PickRF();
        TerminalFunction pickTF = null;// new PickTF();
        RewardFunction moveRF   = null;// new MoveRF();
        TerminalFunction moveTF = null;// new MoveTF();
        RewardFunction baseRF   = new CleanupRF(goalCondition, rewardGoal, rewardBase, rewardNoop, rewardPull);
        TerminalFunction baseTF = new GoalConditionTF(goalCondition);

        Cleanup baseDomainG = new Cleanup(minX, minY, maxX, maxY);
        baseDomainG.setRf(baseRF);
        baseDomainG.setTf(baseTF);
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

        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
        double noopReward = NonprimitiveTask.NOOP_REWARD;

        ActionType aNorth = baseDomain.getAction(Cleanup.ACTION_NORTH);
        ActionType aEast = baseDomain.getAction(Cleanup.ACTION_EAST);
        ActionType aSouth = baseDomain.getAction(Cleanup.ACTION_SOUTH);
        ActionType aWest = baseDomain.getAction(Cleanup.ACTION_WEST);
        ActionType aPull = baseDomain.getAction(Cleanup.ACTION_PULL);
        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
        PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
        PrimitiveTask pull = new PrimitiveTask(aPull, baseDomain);
        Task[] subTasks = {north, south, east, west, pull};
        ActionType aMoveAgentToDoor = pickRoomAgentDomain.getAction(CleanupPick.ACTION_MOVE_AGENT_DOOR);
        StateMapping agentDoorMapper = new AgentDoorMapper();
        PropositionalFunction agentDoorFail = new ObjectInRegionFailPF("AgentDoorFailPF", new String[]{CLASS_AGENT, CLASS_DOOR});
        PropositionalFunction agentDoorGoal = new ObjectInRegionGoalPF("AgentDoorGoalPF", new String[]{CLASS_AGENT, CLASS_DOOR});
        NonprimitiveTask agentToDoor = new NonprimitiveTask(
                subTasks,
                aMoveAgentToDoor,
                moveAgentDoorDomain,
                agentDoorMapper,
                agentDoorFail,
                agentDoorGoal,
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(agentToDoor); }

        StateMapping agentRoomMapper = new AgentRoomMapper();
        ActionType aMoveAgentToRoom = pickRoomAgentDomain.getAction(CleanupPick.ACTION_MOVE_AGENT_ROOM);
        PropositionalFunction agentRoomFail = new ObjectInRegionFailPF("AgentRoomFailPF", new String[]{CLASS_AGENT, CLASS_ROOM});
        PropositionalFunction agentRoomGoal = new ObjectInRegionGoalPF("AgentRoomGoalPF", new String[]{CLASS_AGENT, CLASS_ROOM});
        NonprimitiveTask agentToRoom = new NonprimitiveTask(
                subTasks,
                aMoveAgentToRoom,
                moveAgentRoomDomain,
                agentRoomMapper,
                agentRoomFail,
                agentRoomGoal,
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(agentToRoom); }

        StateMapping blockDoorMapper = new BlockDoorMapper();
        ActionType aMoveBlockToDoor = pickRoomBlockDomain.getAction(CleanupPick.ACTION_MOVE_BLOCK_DOOR);
        PropositionalFunction blockDoorFail = new ObjectInRegionFailPF("BlockDoorFailPF", new String[]{CLASS_BLOCK, CLASS_DOOR});
        PropositionalFunction blockDoorGoal = new ObjectInRegionGoalPF("BlockDoorGoalPF", new String[]{CLASS_BLOCK, CLASS_DOOR});
        NonprimitiveTask blockToDoor = new NonprimitiveTask(
                subTasks,
                aMoveBlockToDoor,
                moveBlockDoorDomain,
                blockDoorMapper,
                blockDoorFail,
                blockDoorGoal,
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(blockToDoor); }

        StateMapping blockRoomMapper = new BlockRoomMapper();
        ActionType aMoveBlockToRoom = pickRoomBlockDomain.getAction(CleanupPick.ACTION_MOVE_BLOCK_ROOM);
        PropositionalFunction blockRoomFail = new ObjectInRegionFailPF("BlockRoomFailPF", new String[]{CLASS_BLOCK, CLASS_ROOM});
        PropositionalFunction blockRoomGoal = new ObjectInRegionGoalPF("BlockRoomGoalPF", new String[]{CLASS_BLOCK, CLASS_ROOM});
        NonprimitiveTask blockToRoom = new NonprimitiveTask(
                subTasks,
                aMoveBlockToRoom,
                moveBlockRoomDomain,
                blockRoomMapper,
                blockRoomFail,
                blockRoomGoal,
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(blockToRoom); }

        ActionType aPickRoomForAgent = rootDomain.getAction(CleanupRoot.ACTION_PICK_ROOM_AGENT);
        Task[] subTasks2 = {agentToDoor, agentToRoom, blockToDoor, blockToRoom};
        NonprimitiveTask pickRoomAgent = new NonprimitiveTask(
                subTasks2,
                aPickRoomForAgent,
                pickRoomAgentDomain,
                new PickRoomAgentMapper(),
                new PickObjectRoomFailPF("PickAgentRoomFailPF", new String[]{CLASS_AGENT, CLASS_ROOM}),
                new PickObjectRoomGoalPF("PickAgentRoomGoalPF", new String[]{CLASS_AGENT, CLASS_ROOM}),
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(pickRoomAgent); }

        ActionType aPickRoomForBlock = rootDomain.getAction(CleanupRoot.ACTION_PICK_ROOM_BLOCK);
        NonprimitiveTask pickRoomBlock = new NonprimitiveTask(
                subTasks2,
                aPickRoomForBlock,
                pickRoomBlockDomain,
                new PickRoomBlockMapper(),
                new PickObjectRoomFailPF("PickBlockRoomFailPF", new String[]{CLASS_BLOCK, CLASS_ROOM}),
                new PickObjectRoomGoalPF("PickBlockRoomGoalPF", new String[]{CLASS_BLOCK, CLASS_ROOM}),
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(pickRoomBlock); }

        ActionType aSolve = new SolveActionType();
        Task[] rootTasks = {pickRoomAgent, pickRoomBlock};
        NonprimitiveTask root = new NonprimitiveTask(
                rootTasks,
                aSolve,
                rootDomain,
                new CleanupRootMapper(),
                new CleanupRootFailPF("CleanupRootFailPF", new String[]{}),
                new CleanupRootGoalPF("CleanupRootGoalPF", goalCondition),
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(root); }

        CleanupGoalDescription[] goals = new CleanupGoalDescription[]{
                new CleanupGoalDescription(new String[]{"block0", "room1"}, baseDomain.propFunction(PF_BLOCK_IN_ROOM)),
        };
        goalCondition.setGoals(goals);

        return root;
    }


}
