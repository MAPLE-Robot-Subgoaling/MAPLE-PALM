package cleanup.hierarchies.tasks.pick;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import cleanup.Cleanup;
import cleanup.hierarchies.tasks.root.CleanupRootModel;
import cleanup.state.CleanupAgent;
import cleanup.state.CleanupBlock;
import cleanup.state.CleanupDoor;
import cleanup.state.CleanupRoom;

public class CleanupPick implements DomainGenerator {

    public static final String ACTION_MOVE_AGENT_DOOR = "moveAgentToDoor";
    public static final String ACTION_MOVE_AGENT_ROOM = "moveAgentToRoom";
    public static final String ACTION_MOVE_BLOCK_DOOR = "moveBlockToDoor";
    public static final String ACTION_MOVE_BLOCK_ROOM = "moveBlockToRoom";

    private RewardFunction rf;
    private TerminalFunction tf;

    public CleanupPick(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    @Override
    public Domain generateDomain() {

        OOSADomain domain = new OOSADomain();

        domain.addStateClass(Cleanup.CLASS_AGENT, CleanupAgent.class);
        domain.addStateClass(Cleanup.CLASS_ROOM, CleanupRoom.class);
        domain.addStateClass(Cleanup.CLASS_BLOCK, CleanupBlock.class);
        domain.addStateClass(Cleanup.CLASS_DOOR, CleanupDoor.class);

        ActionType aMoveAgentToDoor = new ObjectToRegionActionType(
                ACTION_MOVE_AGENT_DOOR, new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_DOOR});
        ActionType aMoveAgentToRoom = new ObjectToRegionActionType(
                ACTION_MOVE_AGENT_ROOM, new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_ROOM});
        ActionType aMoveBlockToDoor = new ObjectToRegionActionType(
                ACTION_MOVE_BLOCK_DOOR, new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_DOOR});
        ActionType aMoveBlockToRoom = new ObjectToRegionActionType(
                ACTION_MOVE_BLOCK_ROOM, new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_ROOM});
        domain.addActionType(aMoveAgentToDoor);
        domain.addActionType(aMoveAgentToRoom);
        domain.addActionType(aMoveBlockToDoor);
        domain.addActionType(aMoveBlockToRoom);


        CleanupRootModel model = new CleanupRootModel();
        FactoredModel fModel = new FactoredModel(model, rf, tf);
        domain.setModel(fModel);

        return domain;
    }

}
