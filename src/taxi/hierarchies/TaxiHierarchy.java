package taxi.hierarchies;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.IdentityMap;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.PrimitiveTask;
import hierarchy.framework.RootTask;
import hierarchy.framework.SolveActionType;
import hierarchy.framework.Task;
import taxi.PutdownActionType;
import taxi.Taxi;
import taxi.functions.amdp.*;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.hierarchies.tasks.bringon.state.BringonStateMapper;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.DropoffStateMapper;
import taxi.hierarchies.tasks.get.BringonActionType;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.get.state.GetParameterizedMapper;
import taxi.hierarchies.tasks.get.state.GetStateMapper;
import taxi.hierarchies.tasks.nav.NavigateActionType;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.functions.rmaxq.BaseGetActionType;
import taxi.functions.rmaxq.BaseGetCompletedPF;
import taxi.functions.rmaxq.BaseGetFailurePF;
import taxi.functions.rmaxq.BaseNavigateActionType;
import taxi.functions.rmaxq.BasePutActionType;
import taxi.functions.rmaxq.BasePutCompletedPF;
import taxi.functions.rmaxq.BasePutFailurePF;
import taxi.hierarchies.tasks.put.DropoffActionType;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.PutStateMapper;
import taxi.hierarchies.tasks.root.GetActionType;
import taxi.hierarchies.tasks.root.PutActionType;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import taxi.hierarchies.tasks.root.state.RootStateMapper;

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
		Taxi taxiDomain;
		if(fickleProbability == 0){
			taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
		}else{
			taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
		}

		//action type domain - not for tasks
		baseDomain = taxiDomain.generateDomain();
		OOSADomain bringonDomain = (new TaxiBringonDomain()).generateDomain();
		OOSADomain dropoffDomain = (new TaxiDropoffDomain()).generateDomain();
		OOSADomain navDomain = (new TaxiNavDomain()).generateDomain();
		OOSADomain getDomain = (new TaxiGetDomain()).generateDomain();
		OOSADomain putDomain = (new TaxiPutDomain()).generateDomain();
		OOSADomain rootDomain = (new TaxiRootDomain()).generateDomain();

		ActionType aNorth = baseDomain.getAction(Taxi.ACTION_NORTH);
		ActionType aEast = baseDomain.getAction(Taxi.ACTION_EAST);
		ActionType aSouth = baseDomain.getAction(Taxi.ACTION_SOUTH);
		ActionType aWest = baseDomain.getAction(Taxi.ACTION_WEST);
		ActionType aPickup = baseDomain.getAction(Taxi.ACTION_PICKUP);
		ActionType aPutdown = new PutdownActionType();
		ActionType aBringon = new BringonActionType();
		ActionType aDropoff = new DropoffActionType();
		ActionType aNavigate = new NavigateActionType();
		ActionType aGet = new GetActionType();
		ActionType aPut = new PutActionType();
		ActionType aSolve = new SolveActionType();
		
		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
		PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
		PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
		PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
		PrimitiveTask putdown = new PrimitiveTask(aPutdown, baseDomain);

		Task[] bringonTasks = {pickup};
		NonprimitiveTask bringon = new NonprimitiveTask(bringonTasks, aBringon, bringonDomain,
				new BringonStateMapper(), new BringonFailurePF(), new BringonCompletedPF());

		Task[] dropoffTasks = {putdown};
		NonprimitiveTask dropoff = new NonprimitiveTask(dropoffTasks, aDropoff, dropoffDomain,
				new DropoffStateMapper(), new DropoffFailurePF(), new DropoffCompletedPF());

		Task[] navTasks = {north, east, south, west};
		StateMapping navMap = new NavStateMapper();
		PropositionalFunction navPF = new NavigateAbstractPF();
		if(plan) {
			navMap = new IdentityMap();
			navPF = new NavigatePF();
		}
		NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, navDomain,
				navMap, navPF, navPF);

		//using parameterized state mapper for get
		Task[] getTasks = {bringon, navigate};
		NonprimitiveTask get = new NonprimitiveTask(getTasks, aGet, getDomain,
                new GetStateMapper(), new GetFailurePF(), new GetCompletedPF());

		Task[] putTasks = {navigate, dropoff};
		NonprimitiveTask put = new NonprimitiveTask(putTasks, aPut, putDomain,
				new PutStateMapper(), new PutFailurePF(), new PutCompletedPF());
		
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
		ActionType aPickup = baseDomain.getAction(Taxi.ACTION_PICKUP);
		ActionType aPutdown = baseDomain.getAction(Taxi.ACTION_PUTDOWN);
		ActionType aNavigate = new BaseNavigateActionType();
		ActionType aGet = new BaseGetActionType();
		ActionType aPut = new BasePutActionType();
		
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

		PropositionalFunction navPF =/* new NavigateAbstractPF()*/ new NavigatePF();
		NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, taxiDomain.generateNavigateDomain(),
				new IdentityMap(), navPF, navPF);
		
		PropositionalFunction pickupFailPF = new BringonFailurePF();
		PropositionalFunction pickupCompPF = new BringonCompletedPF();
		NonprimitiveTask pickupL1 = new NonprimitiveTask(bringonTasks, aPickup, pickupFailPF, pickupCompPF);
		
		PropositionalFunction dropoffFailPF = new DropoffFailurePF();
		PropositionalFunction dropoffCompPF = new DropoffCompletedPF();
		NonprimitiveTask dropoffL1 = new NonprimitiveTask(dropoffTasks, aPutdown, dropoffFailPF, dropoffCompPF);
		
		Task[] getTasks = new Task[]{pickupL1, navigate};
		Task[] putTasks = new Task[]{navigate, dropoffL1};
		
		PropositionalFunction getFailPF = new BaseGetFailurePF();
		PropositionalFunction getCompPF = new BaseGetCompletedPF();
		NonprimitiveTask get = new NonprimitiveTask(getTasks, aGet, getFailPF, getCompPF);
		
		PropositionalFunction putFailPF = new BasePutFailurePF();
		PropositionalFunction putCompPF = new BasePutCompletedPF();
		NonprimitiveTask put = new NonprimitiveTask(putTasks, aPut, putFailPF, putCompPF);
		
		Task[] rootTasks = {get, put};
		Task root = new RootTask(rootTasks, baseDomain, new IdentityMap());
		
		return root;
		
	}
	
	/**
	 * get base taxi domain
	 * @return full base taxi domain
	 */
	public static OOSADomain getBaseDomain(){
		return baseDomain;
	}
}