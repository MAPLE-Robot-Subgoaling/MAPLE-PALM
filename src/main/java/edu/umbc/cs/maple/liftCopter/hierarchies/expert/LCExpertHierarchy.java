package edu.umbc.cs.maple.liftCopter.hierarchies.expert;

import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.NonprimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.PrimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.SolveActionType;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.liftCopter.LiftCopter;
import edu.umbc.cs.maple.liftCopter.hierarchies.LCHierarchy;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.LCGetDomain;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state.GetStateMapper;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.LCNavDomain;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.NavStateMapper;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.LCPutDomain;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.state.PutStateMapper;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.LCRootDomain;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.RootStateMapper;
import edu.umbc.cs.maple.liftCopter.hierarchies.functions.*;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCExpertHierarchy extends LCHierarchy {

    /***
     * creates the standards copter hierarchy and returns the root task
     * @param correctMoveprob the transitionProbability that a movement action will work as expected
     * @return the root task of the copter hierarchy
     */
    @Override
    public Task createHierarchy(double correctMoveprob, boolean plan){
        // Setup copter domain
        LiftCopter copterDomain;

            copterDomain = new LiftCopter(correctMoveprob);

        LCNavDomain navDomainGen = new LCNavDomain();
        // Domains
        OOSADomain rootDomain = (new LCRootDomain()).generateDomain();
        OOSADomain getDomain = (new LCGetDomain()).generateDomain();
        OOSADomain putDomain = (new LCPutDomain()).generateDomain();
        OOSADomain navDomain = navDomainGen.generateDomain();
        baseDomain = copterDomain.generateDomain();
        rootDomain.setModel(null);
        getDomain.setModel(null);
        putDomain.setModel(null);
        navDomain.setModel(null);

        // Navigate Tasks (Primitives used for Put Nav and Get Nav later)
        ActionType aIdle = navDomain.getAction(ACTION_IDLE);
        PrimitiveTask idle = new PrimitiveTask(aIdle, baseDomain);
        //thrust_0.02_1.5
        List<Double> thrustValues = navDomainGen.getThrustValues();
        List<Double> directionValues = navDomainGen.getDirectionValues();
        int numNavPrimitives = 1+thrustValues.size()*directionValues.size();
        Task[] navTasks = new Task[numNavPrimitives];
        navTasks[0] = idle;
        int i = 1;
        for (Double thrustVal: thrustValues) {
            for (Double dirVal: directionValues) {
                ActionType primActType = navDomain.getAction("thrust_"+thrustVal+"_"+dirVal);
                PrimitiveTask primTask = new PrimitiveTask(primActType, baseDomain);
                navTasks[i] = primTask;
                i++;
            }
        }

        double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
        double noopReward = NonprimitiveTask.NOOP_REWARD;

        // Nav (Task used by Get and Put)
        ActionType aNavigate = putDomain.getAction(ACTION_NAV);
        NonprimitiveTask navigate = new NonprimitiveTask(
                navTasks,
                aNavigate,
                navDomain,
                new NavStateMapper(),
                new NavFailurePF(),
                new NavCompletedPF(),
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(navigate); }

        // Pickup
        ActionType aPickup = getDomain.getAction(ACTION_PICKUP);
        PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);

        // Get (Task used by Root)
        ActionType aGet = rootDomain.getAction(ACTION_GET);
        Task[] getTasks = {pickup, navigate};
        NonprimitiveTask get = new NonprimitiveTask(
                getTasks,
                aGet,
                getDomain,
                new GetStateMapper(),
                new GetFailurePF(),
                new GetCompletedPF(),
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(get); }

        // Putdown
        ActionType aPutdown = putDomain.getAction(ACTION_PUTDOWN);
        PrimitiveTask putdown = new PrimitiveTask(aPutdown, baseDomain);

        // Put (Task used by Root)
        ActionType aPut = rootDomain.getAction(ACTION_PUT);
        Task[] putTasks = {putdown, navigate};
        NonprimitiveTask put = new NonprimitiveTask(
                putTasks,
                aPut,
                putDomain,
                new PutStateMapper(),
                new PutFailurePF(),
                new PutCompletedPF(),
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(put); }

        // Root
        ActionType aSolve = new SolveActionType();
        Task[] rootTasks = {get, put};
        NonprimitiveTask root = new NonprimitiveTask(
                rootTasks,
                aSolve,
                rootDomain,
                new RootStateMapper(),
                new RootFailurePF(),
                new RootCompletedPF(),
                defaultReward,
                noopReward
        );
        if (plan) { setupKnownTFRF(root); }

        return root;
    }

    public static void main( String[] args){
        Yaml yaml = new Yaml();
        System.out.println(yaml.dump(new LCExpertHierarchy().createHierarchy( 1.0, true)));
    }

    @Override
    public Task createHierarchy(ExperimentConfig experimentConfig, boolean plan) {
        return createHierarchy(1,true);
    }
}
