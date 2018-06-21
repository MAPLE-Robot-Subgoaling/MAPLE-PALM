package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.model.RewardFunction;
import edu.umbc.cs.maple.cleanup.Cleanup;

public class CleanupMove extends Cleanup {

    public CleanupMove(int minX, int minY, int maxX, int maxY, RewardFunction moveRF, TerminalFunction moveTF) {
        super(minX, minY, maxX, maxY);
        this.setRf(moveRF);
        this.setTf(moveTF);
    }

}
