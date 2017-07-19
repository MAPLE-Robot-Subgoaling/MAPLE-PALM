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
import taxi.Taxi;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction1.state.L1StateMapper;
import taxi.abstraction2.TaxiL2;
import taxi.abstraction2.state.L2StateMapper;
import taxi.amdp.functions.DropoffCompletedPF;
import taxi.amdp.functions.DropoffFailurePF;
import taxi.amdp.functions.GetCompletedPF;
import taxi.amdp.functions.GetFailurePF;
import taxi.amdp.functions.NavigatePF;
import taxi.amdp.functions.PickupCompletedPF;
import taxi.amdp.functions.PickupFailurePF;
import taxi.amdp.functions.PutCompletedPF;
import taxi.amdp.functions.PutFailurePF;
import taxi.amdp.functions.RootPF;
import taxi.rmaxq.functions.BaseGetActionType;
import taxi.rmaxq.functions.BaseGetCompletedPF;
import taxi.rmaxq.functions.BaseGetFailurePF;
import taxi.rmaxq.functions.BaseNavigateActionType;
import taxi.rmaxq.functions.BasePutActionType;
import taxi.rmaxq.functions.BasePutCompletedPF;
import taxi.rmaxq.functions.BasePutFailurePF;
import taxi.state.NavStateMapper;

public class TaxiHierarchy {

	/**
	 * the full base taxi domain
	 */
	private static OOSADomain l0Domian;
	
	/***
	 * creates the standards taxi hierarchy and returns the root task 
	 * @param correctMoveprob the probability that a movement action will work as expected 
	 * @param fickleProbability the probability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createAMDPHierarchy(double correctMoveprob, double fickleProbability){
		Taxi   l0Gen;
		TaxiL1 l1Gen;
		TaxiL2 l2Gen;
		
		if(fickleProbability == 0){
			l0Gen = new Taxi(false, fickleProbability, correctMoveprob);
			l1Gen = new TaxiL1();
			l2Gen = new TaxiL2();
		}else{
			l0Gen = new Taxi(true, fickleProbability, correctMoveprob);
			l1Gen = new TaxiL1(true, fickleProbability);
			l2Gen = new TaxiL2(true, fickleProbability);
		}

		//action type domain - not for tasks
		l0Domian = l0Gen.generateDomain();
		OOSADomain l1Domain = l1Gen.generateDomain();
		OOSADomain l2Domain = l2Gen.generateDomain();
		
		//state mapping function
		StateMapping map0 = new IdentityMap();
		StateMapping mapNav = new NavStateMapper();
		StateMapping map1 = new L1StateMapper();
		StateMapping map2 = new L2StateMapper();
		
		ActionType aNorth = l0Domian.getAction(Taxi.ACTION_NORTH);
		ActionType aEast = l0Domian.getAction(Taxi.ACTION_EAST);
		ActionType aSouth = l0Domian.getAction(Taxi.ACTION_SOUTH);
		ActionType aWest = l0Domian.getAction(Taxi.ACTION_WEST);
		ActionType aPickup = l0Domian.getAction(Taxi.ACTION_PICKUP);
		ActionType aDropoff = l0Domian.getAction(Taxi.ACTION_DROPOFF);
		ActionType aPickupL1 = l1Domain.getAction(TaxiL1.ACTION_L1PICKUP);
		ActionType aDropoffL1 = l1Domain.getAction(TaxiL1.ACTION_L1DROPOFF);
		ActionType aNavigate = l1Domain.getAction(TaxiL1.ACTION_NAVIGATE);
		ActionType aGet = l2Domain.getAction(TaxiL2.ACTION_GET);
		ActionType aPut = l2Domain.getAction(TaxiL2.ACTION_PUT);
		ActionType aSolve = new SolveActionType();
		
		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, l0Domian);
		PrimitiveTask east = new PrimitiveTask(aEast, l0Domian);
		PrimitiveTask south = new PrimitiveTask(aSouth, l0Domian);
		PrimitiveTask wast = new PrimitiveTask(aWest, l0Domian);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, l0Domian);
		PrimitiveTask dropoff = new PrimitiveTask(aDropoff, l0Domian);
		
		Task[] navTasks = {north, east, south, wast};
		Task[] pickupL1Tasks = {pickup};
		Task[] dropoffL1Tasks = {dropoff};
		
		PropositionalFunction navPF = new NavigatePF();
		NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, l0Gen.generateNavigateDomain(),
				mapNav, navPF, navPF);
		
		PropositionalFunction pickupFailPF = new PickupFailurePF();
		PropositionalFunction pickupCompPF = new PickupCompletedPF();
		NonprimitiveTask pickupL1 = new NonprimitiveTask(pickupL1Tasks, aPickupL1, l0Gen.generatePickupDomain(),
				map0, pickupFailPF, pickupCompPF);
		
		PropositionalFunction dropoffFailPF = new DropoffFailurePF();
		PropositionalFunction dropoffCompPF = new DropoffCompletedPF();
		NonprimitiveTask dropoffL1 = new NonprimitiveTask(dropoffL1Tasks, aDropoffL1, l0Gen.generateDropOffDomain(),
				map0, dropoffFailPF, dropoffCompPF);
		
		Task[] getTasks = {pickupL1, navigate};
		Task[] putTasks = {navigate, dropoffL1};
		
		PropositionalFunction getFailPF = new GetFailurePF();
		PropositionalFunction getCompPF = new GetCompletedPF();
		NonprimitiveTask get = new NonprimitiveTask(getTasks, aGet, l1Gen.generateGetDomain(),
				map1, getFailPF, getCompPF);
		
		PropositionalFunction putFailPF = new PutFailurePF();
		PropositionalFunction putCompPF = new PutCompletedPF();
		NonprimitiveTask put = new NonprimitiveTask(putTasks, aPut, l1Gen.generatePutDomain(),
				map1, putFailPF, putCompPF);
		
		Task[] rootTasks = {get, put};
		
		PropositionalFunction rootPF = new RootPF();
		Task root = new NonprimitiveTask(rootTasks, aSolve, l2Domain, map2, rootPF, rootPF);
		return root;
	}

	/**
	 * creates a taxi hierarchy with no abstractions 
	 * @param correctMoveprob the probability that a movement action will work as expected 
	 * @param fickleProbability the probability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createRMAXQHierarchy(double correctMoveprob, double fickleProbability){
		Taxi l0Gen;
		
		if(fickleProbability == 0){
			l0Gen = new Taxi(false, fickleProbability, correctMoveprob);
		}else{
			l0Gen = new Taxi(true, fickleProbability, correctMoveprob);
		}

		//action type domain - not for tasks
		l0Domian = l0Gen.generateDomain();
		
		StateMapping map0 = new IdentityMap();

		ActionType aNorth = l0Domian.getAction(Taxi.ACTION_NORTH);
		ActionType aEast = l0Domian.getAction(Taxi.ACTION_EAST);
		ActionType aSouth = l0Domian.getAction(Taxi.ACTION_SOUTH);
		ActionType aWest = l0Domian.getAction(Taxi.ACTION_WEST);
		ActionType aPickup = l0Domian.getAction(Taxi.ACTION_PICKUP);
		ActionType aDropoff = l0Domian.getAction(Taxi.ACTION_DROPOFF);
		ActionType aNavigate = new BaseNavigateActionType();
		ActionType aGet = new BaseGetActionType();
		ActionType aPut = new BasePutActionType();
		
		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, l0Domian);
		PrimitiveTask east = new PrimitiveTask(aEast, l0Domian);
		PrimitiveTask south = new PrimitiveTask(aSouth, l0Domian);
		PrimitiveTask wast = new PrimitiveTask(aWest, l0Domian);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, l0Domian);
		PrimitiveTask dropoff = new PrimitiveTask(aDropoff, l0Domian);
		
		Task[] navTasks = new Task[]{north, east, south, wast};
		Task[] pickupL1Tasks = new Task[]{pickup};
		Task[] dropoffL1Tasks = new Task[]{dropoff};
		
		PropositionalFunction navPF = new NavigatePF();
		NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, l0Gen.generateNavigateDomain(),
				map0, navPF, navPF);
		
		PropositionalFunction pickupFailPF = new PickupFailurePF();
		PropositionalFunction pickupCompPF = new PickupCompletedPF();
		NonprimitiveTask pickupL1 = new NonprimitiveTask(pickupL1Tasks, aPickup, pickupFailPF, pickupCompPF);
		
		PropositionalFunction dropoffFailPF = new DropoffFailurePF();
		PropositionalFunction dropoffCompPF = new DropoffCompletedPF();
		NonprimitiveTask dropoffL1 = new NonprimitiveTask(dropoffL1Tasks, aDropoff, dropoffFailPF, dropoffCompPF);
		
		Task[] getTasks = new Task[]{pickupL1, navigate};
		Task[] putTasks = new Task[]{navigate, dropoffL1};
		
		PropositionalFunction getFailPF = new BaseGetFailurePF();
		PropositionalFunction getCompPF = new BaseGetCompletedPF();
		NonprimitiveTask get = new NonprimitiveTask(getTasks, aGet, getFailPF, getCompPF);
		
		PropositionalFunction putFailPF = new BasePutFailurePF();
		PropositionalFunction putCompPF = new BasePutCompletedPF();
		NonprimitiveTask put = new NonprimitiveTask(putTasks, aPut, putFailPF, putCompPF);
		
		Task[] rootTasks = {get, put};
		Task root = new RootTask(rootTasks, l0Domian, map0); 
		
		return root;
		
	}
	
	/**
	 * get base taxi domain
	 * @return full base taxi domain
	 */
	public static OOSADomain getBaseDomain(){
		return l0Domian;
	}
}
