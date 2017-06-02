package taxi.ramdp;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.framework.IdentityMap;
import ramdp.framework.PrimitiveTask;
import ramdp.framework.RootTask;
import ramdp.framework.Task;
import taxi.TaxiDomain;
import taxi.TaxiRewardFunction;
import taxi.TaxiTerminationFunction;
import taxi.amdp.level1.TaxiL1Domain;
import taxi.amdp.level1.TaxiL1TerminalFunction;
import taxi.amdp.level1.state.L1StateMapper;
import taxi.amdp.level2.TaxiL2Domain;
import taxi.amdp.level2.TaxiL2TerminalFunction;
import taxi.amdp.level2.state.L2StateMapper;
import taxi.ramdp.tasks.DropoffL1Task;
import taxi.ramdp.tasks.GetTask;
import taxi.ramdp.tasks.NavigateTask;
import taxi.ramdp.tasks.PickupL1Task;
import taxi.ramdp.tasks.PutTask;
import taxi.state.TaxiState;

public class TaxiHierarchy {

	private static OOSADomain l0Domain;
	/**
	 * Creates the standard task hierarchy
	 * @param s The initial state
	 * @return a root of the taxi hierarchy
	 */
	public static Task createHierarchy(TaxiState s, boolean fickle){
		//domains generators
		TerminalFunction tf0 = new TaxiTerminationFunction();
		RewardFunction rf0 = new TaxiRewardFunction(s.passengers.size(), tf0);
		TaxiDomain baseGenerator = new TaxiDomain(rf0, tf0);
		baseGenerator.setFickleTaxi(fickle);
		baseGenerator.setIncludeFuel(false);
		if(fickle)
			baseGenerator.setTransitionDynamicsLikeFickleTaxiProlem();
		
		TerminalFunction tf1 = new TaxiL1TerminalFunction();
		RewardFunction rf1 = new GoalBasedRF(tf1);
		TaxiL1Domain l1Generator = new TaxiL1Domain(rf1, tf1);
		
		TerminalFunction tf2 = new TaxiL2TerminalFunction();
		RewardFunction rf2 = new GoalBasedRF(tf2);
		TaxiL2Domain l2Generator = new TaxiL2Domain(rf2, tf2);
		
		//domains
		l0Domain = baseGenerator.generateDomain();
		OOSADomain l1Domain = l1Generator.generateDomain();
		OOSADomain l2Domain = l2Generator.generateDomain();
		
		//state mappers
		StateMapping l0Map = new IdentityMap();
		StateMapping l1Map = new L1StateMapper();
		StateMapping l2Map = new L2StateMapper();
		
		//actions
		ActionType pickup = l0Domain.getAction(TaxiDomain.ACTION_PICKUP);
		ActionType dropoff = l0Domain.getAction(TaxiDomain.ACTION_DROPOFF);
		ActionType north = l0Domain.getAction(TaxiDomain.ACTION_NORTH);
		ActionType east = l0Domain.getAction(TaxiDomain.ACTION_EAST);
		ActionType south = l0Domain.getAction(TaxiDomain.ACTION_SOUTH);
		ActionType west = l0Domain.getAction(TaxiDomain.ACTION_WEST);
		ActionType pickupL1 = l1Domain.getAction(TaxiL1Domain.ACTION_PICKUPL1);
		ActionType dropoffL1 = l1Domain.getAction(TaxiL1Domain.ACTION_PUTDOWNL1);
		ActionType navigate = l1Domain.getAction(TaxiL1Domain.ACTION_NAVIGATE);
		ActionType get = l2Domain.getAction(TaxiL2Domain.ACTION_GET);
		ActionType put = l2Domain.getAction(TaxiL2Domain.ACTION_PUT);
		
		//tasks
		PrimitiveTask pickupL0Task = new PrimitiveTask(pickup, l0Domain);
		PrimitiveTask dropoffTask = new PrimitiveTask(dropoff, l0Domain);
		PrimitiveTask northTask = new PrimitiveTask(north, l0Domain);
		PrimitiveTask eastTask = new PrimitiveTask(east, l0Domain);
		PrimitiveTask southTask = new PrimitiveTask(south, l0Domain);
		PrimitiveTask westTask = new PrimitiveTask(west, l0Domain);
		
		Task[] pickupsub = new Task[]{pickupL0Task};
		Task[] navSubs = new Task[]{northTask, eastTask, southTask, westTask};
		Task[] dropoffsub = new Task[]{dropoffTask};
		
		//for non primitive domains, it should be the domain which subtask are executed in
		OOSADomain pickupDomain = baseGenerator.generateDomain();
		pickupDomain.clearActionTypes();
		pickupDomain.addActionType(pickup);
		PickupL1Task pickupl1Task = new PickupL1Task(pickupsub, pickupL1, pickupDomain, l0Map);
		
		OOSADomain navDomain = baseGenerator.generateDomain();
		navDomain.clearActionTypes();
		navDomain.addActionTypes(north, east, south, west);
		NavigateTask navigateTask = new NavigateTask(navSubs, navigate, navDomain, l0Map);
		
		OOSADomain dropoffDomain = baseGenerator.generateDomain();
		dropoffDomain.clearActionTypes();
		dropoffDomain.addActionType(dropoff);
		DropoffL1Task dropoffL1Task = new DropoffL1Task(dropoffsub, dropoffL1, dropoffDomain, l0Map);
		
		Task[] getSubs = new Task[]{pickupl1Task, navigateTask};
		Task[] putSubs = new Task[]{navigateTask, dropoffL1Task};
		
		OOSADomain getDoman = l1Generator.generateDomain();
		getDoman.clearActionTypes();
		getDoman.addActionTypes(pickupL1, navigate);
		GetTask getTask = new GetTask(getSubs, get, getDoman, l1Map);
		
		OOSADomain putDomain = l1Generator.generateDomain();
		putDomain.clearActionTypes();
		putDomain.addActionTypes(navigate, dropoffL1);
		PutTask putTask = new PutTask(putSubs, put, putDomain, l1Map);
		
		Task[] rootsubs = new Task[]{getTask, putTask};
		RootTask root = new RootTask(rootsubs, l2Domain, l2Map);
		return root;
	}
	
	public static OOSADomain getGroundDomain(){
		return l0Domain;
	}
}
