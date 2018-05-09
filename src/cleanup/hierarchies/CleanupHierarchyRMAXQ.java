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
import cleanup.hierarchies.tasks.move.BaseObjectToRegionActionType;
import cleanup.hierarchies.tasks.move.CleanupMove;
import cleanup.hierarchies.tasks.move.ObjectInRegionFailPF;
import cleanup.hierarchies.tasks.move.ObjectInRegionGoalPF;
import cleanup.hierarchies.tasks.pick.CleanupPick;
import cleanup.hierarchies.tasks.pick.PickRF;
import cleanup.hierarchies.tasks.pick.PickTF;
import cleanup.hierarchies.tasks.root.CleanupRoot;
import cleanup.hierarchies.tasks.root.CleanupRootGoalPF;
import config.cleanup.CleanupConfig;
import hierarchy.framework.*;

import static cleanup.Cleanup.*;

public class CleanupHierarchyRMAXQ extends CleanupHierarchy {

    public CleanupHierarchyRMAXQ() {

    }

    public Task createRMAXQHierarchy(CleanupConfig config){

        int minX = config.minX;
        int minY = config.minY;
        int maxX = config.maxX;
        int maxY = config.maxY;
        double rewardGoal = config.rewardGoal; // 500;
        double rewardBase = config.rewardBase; // -1;
        double rewardNoop = config.rewardNoop; // -1;
        double rewardPull = config.rewardPull; // 0;

        CleanupGoal goalCondition = new CleanupGoal();

        RewardFunction rootRF = new PickRF();//new CleanupRF(goalCondition, rewardGoal, rewardBase, rewardNoop, rewardPull);
        TerminalFunction rootTF = new PickTF();//new GoalConditionTF(goalCondition);
        RewardFunction pickRF = new PickRF();
        TerminalFunction pickTF = new PickTF();
        RewardFunction moveRF = new CleanupRF(goalCondition, rewardGoal, rewardBase, rewardNoop, rewardPull);;//new MoveRF();
        TerminalFunction moveTF = new GoalConditionTF(goalCondition);//new MoveTF();

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

        ActionType aMoveAgentToDoor = new BaseObjectToRegionActionType(CleanupPick.ACTION_MOVE_AGENT_DOOR, new String[]{CLASS_AGENT, CLASS_DOOR});
        ActionType aMoveAgentToRoom = new BaseObjectToRegionActionType(CleanupPick.ACTION_MOVE_AGENT_ROOM, new String[]{CLASS_AGENT, CLASS_ROOM});//new BaseAgentRoomActionType(); //pickRoomAgentDomain.getAction(CleanupPick.ACTION_MOVE_AGENT_ROOM);
        ActionType aMoveBlockToDoor = new BaseObjectToRegionActionType(CleanupPick.ACTION_MOVE_BLOCK_DOOR, new String[]{CLASS_BLOCK, CLASS_DOOR});//new BaseBlockDoorActionType(); //pickRoomBlockDomain.getAction(CleanupPick.ACTION_MOVE_BLOCK_DOOR);
        ActionType aMoveBlockToRoom = new BaseObjectToRegionActionType(CleanupPick.ACTION_MOVE_BLOCK_ROOM, new String[]{CLASS_BLOCK, CLASS_ROOM});//new BaseBlockRoomActionType(); //pickRoomBlockDomain.getAction(CleanupPick.ACTION_MOVE_BLOCK_ROOM);

        ActionType aPickRoomForAgent = new BaseObjectToRegionActionType(CleanupRoot.ACTION_PICK_ROOM_AGENT, new String[]{CLASS_AGENT, CLASS_ROOM});//new BasePickRoomAgentActionType(); // rootDomain.getAction(CleanupRoot.ACTION_PICK_ROOM_AGENT);
        ActionType aPickRoomForBlock = new BaseObjectToRegionActionType(CleanupRoot.ACTION_PICK_ROOM_BLOCK, new String[]{CLASS_BLOCK, CLASS_ROOM});//new BasePickRoomBlockActionType(); // rootDomain.getAction(CleanupRoot.ACTION_PICK_ROOM_BLOCK);
        ActionType aSolve = new SolveActionType();

        PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
        PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
        PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
        PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
        PrimitiveTask pull = new PrimitiveTask(aPull, baseDomain);

        Task[] subTasks = {north, south, east, west, pull};

        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
        double noopReward = NonprimitiveTask.NOOP_REWARD;

        StateMapping agentDoorMapper = new IdentityMap();
        PropositionalFunction agentDoorFail = new ObjectInRegionFailPF("AgentDoorFail", new String[]{CLASS_AGENT, CLASS_DOOR});
        PropositionalFunction agentDoorGoal = new ObjectInRegionGoalPF("AgentDoorGoal", new String[]{CLASS_AGENT, CLASS_DOOR});
        NonprimitiveTask agentToDoor = new NonprimitiveTask(
                subTasks,
                aMoveAgentToDoor,
                moveAgentDoorDomain,
                agentDoorMapper,
                agentDoorFail,
                agentDoorGoal, defaultReward, noopReward
        );

        StateMapping agentRoomMapper = new IdentityMap();
        PropositionalFunction agentRoomFail = new ObjectInRegionFailPF("AgentRoomFail", new String[]{CLASS_AGENT, CLASS_ROOM});
        PropositionalFunction agentRoomGoal = new ObjectInRegionGoalPF("AgentRoomGoal", new String[]{CLASS_AGENT, CLASS_ROOM});
        NonprimitiveTask agentToRoom = new NonprimitiveTask(
                subTasks,
                aMoveAgentToRoom,
                moveAgentRoomDomain,
                agentRoomMapper,
                agentRoomFail,
                agentRoomGoal, defaultReward, noopReward
        );

        StateMapping blockDoorMapper = new IdentityMap();
        PropositionalFunction blockDoorFail = new ObjectInRegionFailPF("BlockDoorFail", new String[]{CLASS_BLOCK, CLASS_DOOR});
        PropositionalFunction blockDoorGoal = new ObjectInRegionGoalPF("BlockDoorGoal", new String[]{CLASS_BLOCK, CLASS_DOOR});
        NonprimitiveTask blockToDoor = new NonprimitiveTask(
                subTasks,
                aMoveBlockToDoor,
                moveBlockDoorDomain,
                blockDoorMapper,
                blockDoorFail,
                blockDoorGoal, defaultReward, noopReward
        );

        StateMapping blockRoomMapper = new IdentityMap();
        PropositionalFunction blockRoomFail = new ObjectInRegionFailPF("BlockRoomFail", new String[]{CLASS_BLOCK, CLASS_ROOM});
        PropositionalFunction blockRoomGoal = new ObjectInRegionGoalPF("BlockRoomGoal", new String[]{CLASS_BLOCK, CLASS_ROOM});
        NonprimitiveTask blockToRoom = new NonprimitiveTask(
                subTasks,
                aMoveBlockToRoom,
                moveBlockRoomDomain,
                blockRoomMapper,
                blockRoomFail,
                blockRoomGoal, defaultReward, noopReward
        );

        StateMapping pickRoomAgentMapper = new IdentityMap();
//        PropositionalFunction pickRoomAgentGoal = new PickObjectRoomGoalPF("PickAgentRoomPF", new String[]{CLASS_AGENT, CLASS_ROOM});
        PropositionalFunction pickRoomAgentFail = new ObjectInRegionGoalPF("PickAgentRoomFail", new String[]{CLASS_AGENT, CLASS_ROOM});
        PropositionalFunction pickRoomAgentGoal = new ObjectInRegionGoalPF("PickAgentRoomGoal", new String[]{CLASS_AGENT, CLASS_ROOM});
        Task[] subTasks2 = {agentToDoor, agentToRoom, blockToDoor, blockToRoom};
        NonprimitiveTask pickRoomAgent = new NonprimitiveTask(
                subTasks2,
                aPickRoomForAgent,
                pickRoomAgentDomain,
                pickRoomAgentMapper,
                pickRoomAgentFail,
                pickRoomAgentGoal, defaultReward, noopReward
        );

        StateMapping pickRoomBlockMapper = new IdentityMap();
        PropositionalFunction pickRoomBlockFail = new ObjectInRegionGoalPF("PickBlockRoomFail", new String[]{CLASS_BLOCK, CLASS_ROOM});
        PropositionalFunction pickRoomBlockGoal = new ObjectInRegionGoalPF("PickBlockRoomGoal", new String[]{CLASS_BLOCK, CLASS_ROOM});
        NonprimitiveTask pickRoomBlock = new NonprimitiveTask(
                subTasks2,
                aPickRoomForBlock,
                pickRoomBlockDomain,
                pickRoomBlockMapper,
                pickRoomBlockFail,
                pickRoomBlockGoal, defaultReward, noopReward
        );

        Task[] rootTasks = {pickRoomAgent, pickRoomBlock};

        StateMapping rootMapper = new IdentityMap();
        PropositionalFunction rootFail = new CleanupRootGoalPF("CleanupRootFail", goalCondition);
        PropositionalFunction rootGoal = new CleanupRootGoalPF("CleanupRootGoal", goalCondition);
        Task root = new NonprimitiveTask(
                rootTasks,
                aSolve,
                rootDomain,
                rootMapper,
                rootFail,
                rootGoal, defaultReward, noopReward
        );

        return root;

    }
}
