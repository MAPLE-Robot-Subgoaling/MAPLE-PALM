package cleanup.hierarchies.tasks.move;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.model.RewardFunction;
import cleanup.Cleanup;

public class CleanupMove extends Cleanup {

    public CleanupMove(int minX, int minY, int maxX, int maxY, RewardFunction moveRF, TerminalFunction moveTF) {
        super(minX, minY, maxX, maxY);
        this.setRf(moveRF);
        this.setTf(moveTF);
    }

}
