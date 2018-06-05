package taxi.hierarchies;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.*;
import taxi.Taxi;
import taxi.functions.amdp.*;
import taxi.hierGen.Task5.state.Task5StateMapper;
import taxi.hierGen.Task7.state.Task7StateMapper;
import taxi.hierGen.Task7.state.TaxiHierGenTask7State;
import taxi.hierGen.actions.HierGenDropoffActiontype;
import taxi.hierGen.actions.HierGenPickupActiontype;
import taxi.hierGen.actions.HierGenTask5ActionType;
import taxi.hierGen.functions.FailureFunction;
import taxi.hierGen.functions.HierGenRootCompleted;
import taxi.hierGen.functions.HierGenTask5Completed;
import taxi.hierGen.functions.HierGenTask7Completed;
import taxi.hierGen.root.state.HierGenRootStateMapper;
import taxi.hierGen.root.state.TaxiHierGenRootState;
import taxi.hierarchies.tasks.NavigateActionType;
import taxi.hierarchies.tasks.get.GetPickupActionType;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.get.state.GetStateMapper;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.hierarchies.tasks.put.PutPutdownActionType;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.PutStateMapper;
import taxi.hierarchies.tasks.root.GetActionType;
import taxi.hierarchies.tasks.root.PutActionType;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import taxi.hierarchies.tasks.root.state.RootStateMapper;
import taxi.rmaxq.functions.BaseRootPF;

import static taxi.TaxiConstants.*;

public abstract class TaxiHierarchy extends Hierarchy {

    /**
     * the full base taxi domain
     */
    public static OOSADomain baseDomain;

    /**
     * get base taxi domain
     * @return full base taxi domain
     */
    public static OOSADomain getBaseDomain(){
        return baseDomain;
    }

    public abstract Task createHierarchy(double correctMoveprob, double fickleProbability, boolean plan);

}