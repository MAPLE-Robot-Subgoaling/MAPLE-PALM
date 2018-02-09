package taxi.hierarchies;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.*;
import taxi.PickupActionType;
import taxi.PutdownActionType;
import taxi.Taxi;
import taxi.functions.amdp.*;
import taxi.functions.rmaxq.*;
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
import taxi.hierarchies.tasks.bringon.BringonPickupActionType;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.hierarchies.tasks.bringon.state.BringonStateMapper;
import taxi.hierarchies.tasks.dropoff.DropoffPutdownActionType;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.DropoffStateMapper;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.get.state.GetStateMapper;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.PutStateMapper;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import taxi.hierarchies.tasks.root.state.RootStateMapper;
import taxi.rmaxq.functions.BaseRootPF;

public class TaxiHierarchy {

	/**
	 * the full base taxi domain
	 */
	private static OOSADomain baseDomain;
	
	/***
	 * creates the standards taxi hierarchy and returns the root task
	 * @param correctMoveprob the probability that a movement action will work as expected
	 * @param fickleProbability the probability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createAMDPHierarchy(double correctMoveprob, double fickleProbability, boolean plan){
		// Setup taxi domain
		Taxi taxiDomain;
		if(fickleProbability == 0){
			taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
		}else{
			taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
		}

		// Domains
		OOSADomain rootDomain = (new TaxiRootDomain()).generateDomain();
		OOSADomain getDomain = (new TaxiGetDomain()).generateDomain();
		OOSADomain putDomain = (new TaxiPutDomain()).generateDomain();
		OOSADomain navDomain = (new TaxiNavDomain()).generateDomain();
		OOSADomain bringonDomain = (new TaxiBringonDomain()).generateDomain();
		OOSADomain dropoffDomain = (new TaxiDropoffDomain()).generateDomain();
		baseDomain = taxiDomain.generateDomain();

		// Navigate Tasks (Primitives used for Put Nav and Get Nav later)
		ActionType aNorth = navDomain.getAction(Taxi.ACTION_NORTH);
		PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
		ActionType aEast = navDomain.getAction(Taxi.ACTION_EAST);
		PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
		ActionType aSouth = navDomain.getAction(Taxi.ACTION_SOUTH);
		PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
		ActionType aWest = navDomain.getAction(Taxi.ACTION_WEST);
		PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
		Task[] navTasks = {north, east, south, west};

		// Nav (Task used by Get and Put)
		ActionType aNavigate = putDomain.getAction(TaxiPutDomain.ACTION_NAV);
		NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, navDomain,
				new NavStateMapper(), new NavFailurePF(), new NavCompletedPF());

		// Pickup (Primitive used by Bringon)
		ActionType aPickup = bringonDomain.getAction(Taxi.ACTION_PICKUP);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);

		// Bringon (Task used by Get)
		ActionType aBringon = getDomain.getAction(TaxiGetDomain.ACTION_BRINGON);
		Task[] bringonTasks = {pickup};
		NonprimitiveTask bringon = new NonprimitiveTask(bringonTasks, aBringon, bringonDomain,
				new BringonStateMapper(), new BringonFailurePF(), new BringonCompletedPF());

		// Get (Task used by Root)
		ActionType aGet = rootDomain.getAction(TaxiRootDomain.ACTION_GET);
		Task[] getTasks = {bringon, navigate};
		NonprimitiveTask get = new NonprimitiveTask(getTasks, aGet, getDomain,
				new GetStateMapper(), new GetFailurePF(), new GetCompletedPF());

		// Putdown (Primitive used by Dropoff)
		ActionType aPutdown = dropoffDomain.getAction(Taxi.ACTION_PUTDOWN);
		PrimitiveTask putdown = new PrimitiveTask(aPutdown, baseDomain);

		// Dropoff (Task used by Put)
		ActionType aDropoff = putDomain.getAction(TaxiPutDomain.ACTION_DROPOFF);
		Task[] dropoffTasks = {putdown};
		NonprimitiveTask dropoff = new NonprimitiveTask(dropoffTasks, aDropoff, dropoffDomain,
				new DropoffStateMapper(), new DropoffFailurePF(), new DropoffCompletedPF());

		// Put (Task used by Root)
		ActionType aPut = rootDomain.getAction(TaxiRootDomain.ACTION_PUT);
		Task[] putTasks = {dropoff, navigate};
		NonprimitiveTask put = new NonprimitiveTask(putTasks, aPut, putDomain,
				new PutStateMapper(), new PutFailurePF(), new PutCompletedPF());

		// Root
		ActionType aSolve = new SolveActionType();
		Task[] rootTasks = {get, put};
		NonprimitiveTask root = new NonprimitiveTask(rootTasks, aSolve, rootDomain,
				new RootStateMapper(), new RootPF(), new RootPF());
		
		return root;
	}

	/**
	 * creates a taxi hierarchy with no abstractions 
	 * @param correctMoveprob the probability that a movement action will work as expected 
	 * @param fickleProbability the probability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createRMAXQHierarchy(double correctMoveprob, double fickleProbability){
		Taxi taxiDomain;
		
		if(fickleProbability == 0){
			taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
		}else{
			taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
		}

		//action type domain - not for tasks
		baseDomain = taxiDomain.generateDomain();

		ActionType aNorth = baseDomain.getAction(Taxi.ACTION_NORTH);
		ActionType aEast = baseDomain.getAction(Taxi.ACTION_EAST);
		ActionType aSouth = baseDomain.getAction(Taxi.ACTION_SOUTH);
		ActionType aWest = baseDomain.getAction(Taxi.ACTION_WEST);
		ActionType aPickup = new PickupActionType(Taxi.ACTION_PICKUP, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aPutdown = new PutdownActionType(Taxi.ACTION_PUTDOWN, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aBrignonPickup = new BringonPickupActionType(Taxi.ACTION_PICKUP, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aDropoffPutdown = new DropoffPutdownActionType(Taxi.ACTION_PUTDOWN, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aNavigate = new BaseNavigateActionType("Nav", new String[]{Taxi.CLASS_LOCATION});
		ActionType aGet = new BaseGetActionType();
		ActionType aPut = new BasePutActionType();
		ActionType aSolve = new SolveActionType();

		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
		PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
		PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
		PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
		PrimitiveTask dropoff = new PrimitiveTask(aPutdown, baseDomain);
		
		Task[] navTasks = new Task[]{north, east, south, wast};
		Task[] bringonTasks = new Task[]{pickup};
		Task[] dropoffTasks = new Task[]{dropoff};

		PropositionalFunction navFailPF = new BaseNavigateFailurePF();
		PropositionalFunction navCompPF = new BaseNavigateCompletedPF();
		NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, taxiDomain.generateNavigateDomain(),
				new IdentityMap(), navFailPF, navCompPF);

		Task[] getTasks = new Task[]{pickup, navigate};
		Task[] putTasks = new Task[]{navigate, dropoff};
		
		PropositionalFunction getFailPF = new BaseGetFailurePF();
		PropositionalFunction getCompPF = new BaseGetCompletedPF();
		NonprimitiveTask get = new NonprimitiveTask(getTasks, aGet, getFailPF, getCompPF);
		
		PropositionalFunction putFailPF = new BasePutFailurePF();
		PropositionalFunction putCompPF = new BasePutCompletedPF();
		NonprimitiveTask put = new NonprimitiveTask(putTasks, aPut, putFailPF, putCompPF);
		
		Task[] rootTasks = {get, put};
		PropositionalFunction rootPF = new BaseRootPF();
		OOSADomain baseActual = taxiDomain.generateDomain();
		baseActual.clearActionTypes();
		baseActual.addActionTypes(
				aNorth,
				aEast,
				aSouth,
				aWest,
				aPickup,
				aPutdown
		);
		Task root = new NonprimitiveTask(rootTasks, aSolve, baseActual, new IdentityMap(), rootPF, rootPF);

		return root;
		
	}
	
	/**
	 * get base taxi domain
	 * @return full base taxi domain
	 */
	public static OOSADomain getBaseDomain(){
		return baseDomain;
	}

	/***
	 * creates the hiergen taxi hierarchy and returns the root task
	 * @param correctMoveprob the probability that a movement action will work as expected
	 * @param fickleProbability the probability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createHierGenHierarchy(double correctMoveprob, double fickleProbability) {
		Taxi taxiDomain;
		if (fickleProbability == 0) {
			taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
		} else {
			taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
		}

		baseDomain = taxiDomain.generateDomain();

		ActionType aNorth = baseDomain.getAction(Taxi.ACTION_NORTH);
		ActionType aEast = baseDomain.getAction(Taxi.ACTION_EAST);
		ActionType aSouth = baseDomain.getAction(Taxi.ACTION_SOUTH);
		ActionType aWest = baseDomain.getAction(Taxi.ACTION_WEST);
		ActionType aPickup = new HierGenPickupActiontype(Taxi.ACTION_PICKUP, new String[]{TaxiHierGenTask7State.CLASS_TASK7_PASSENGER});
		ActionType aPutdown = new HierGenDropoffActiontype(Taxi.ACTION_PUTDOWN, new String[]{TaxiHierGenRootState.CLASS_ROOT_PASSENGER});
		ActionType aTask5 = new HierGenTask5ActionType();;
		ActionType aTask7 = new UniversalActionType("task_7");
		ActionType asolve = new SolveActionType();

		//state mapper
		StateMapping task5Map = new Task5StateMapper();
		StateMapping task7Map = new Task7StateMapper();
		StateMapping rootMap = new HierGenRootStateMapper();

		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
		PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
		PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
		PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
		PrimitiveTask dropoff = new PrimitiveTask(aPutdown, baseDomain);

		Task[] task5Children = {north, east, south, wast};
		PropositionalFunction task5CompletedPF = new HierGenTask5Completed();
		PropositionalFunction task5FailPF = new FailureFunction();
		NonprimitiveTask task5 = new NonprimitiveTask(task5Children, aTask5, task5Map,
				task5FailPF, task5CompletedPF);

		Task[] task7Children = {task5, pickup};
		PropositionalFunction task7CompletedPF = new HierGenTask7Completed();
		PropositionalFunction task7FailPF = new FailureFunction();
		NonprimitiveTask task7 = new NonprimitiveTask(task7Children, aTask7, task7Map,
				task7FailPF, task7CompletedPF);

		Task[] rootChildren = {task7, dropoff, task5};
		PropositionalFunction rootCompletedPF = new HierGenRootCompleted();
		PropositionalFunction rootFailPF = new FailureFunction();
		NonprimitiveTask root = new NonprimitiveTask(rootChildren, asolve, rootMap,
				rootFailPF, rootCompletedPF);

		return root;
	}
}