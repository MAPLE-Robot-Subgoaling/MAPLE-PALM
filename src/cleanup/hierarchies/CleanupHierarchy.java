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
import config.cleanup.CleanupConfig;
import hierarchy.framework.*;

import static cleanup.Cleanup.*;

public abstract class CleanupHierarchy {

    protected OOSADomain baseDomain;

    public void setBaseDomain(OOSADomain baseDomain) {
        this.baseDomain = baseDomain;
    }

    public OOSADomain getBaseDomain(){
        return baseDomain;
    }

}