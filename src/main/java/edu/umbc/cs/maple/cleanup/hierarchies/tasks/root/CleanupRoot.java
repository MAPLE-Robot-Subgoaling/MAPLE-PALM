package edu.umbc.cs.maple.cleanup.hierarchies.tasks.root;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickRoomForObjectActionType;
import edu.umbc.cs.maple.cleanup.state.CleanupAgent;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;

public class CleanupRoot implements DomainGenerator {

    public static final String ACTION_PICK_ROOM_AGENT = "pickRoomForAgent";
    public static final String ACTION_PICK_ROOM_BLOCK = "pickRoomForBlock";

    private RewardFunction rf;
    private TerminalFunction tf;

    public CleanupRoot(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    @Override
    public OOSADomain generateDomain() {

        OOSADomain domain = new OOSADomain();

        domain.addStateClass(Cleanup.CLASS_AGENT, CleanupAgent.class);
        domain.addStateClass(Cleanup.CLASS_ROOM, CleanupRoom.class);
        domain.addStateClass(Cleanup.CLASS_BLOCK, CleanupBlock.class);


        ActionType aPickRoomForAgent = new PickRoomForObjectActionType(
                ACTION_PICK_ROOM_AGENT, new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_ROOM});
        ActionType aPickRoomForBlock = new PickRoomForObjectActionType(
                ACTION_PICK_ROOM_BLOCK, new String[]{Cleanup.CLASS_BLOCK, Cleanup.CLASS_ROOM});
        domain.addActionType(aPickRoomForAgent);
        domain.addActionType(aPickRoomForBlock);

        CleanupRootModel model = new CleanupRootModel();
        FactoredModel fModel = new FactoredModel(model, rf, tf);
        domain.setModel(fModel);

        return domain;
    }


}
