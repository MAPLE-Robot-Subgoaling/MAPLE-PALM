package taxi.hierarchies;

import com.sun.org.apache.bcel.internal.generic.L2D;

import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.PrimitiveTask;
import hierarchy.framework.Task;
import taxi.Taxi;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction2.TaxiL2;

public class TaxiHierarchy {

	private static OOSADomain l0Domian;
	
	public static Task createAMDP(double correctMoveprob, double fickleProbability){
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
		ActionType aput = l2Domain.getAction(TaxiL2.ACTION_PUT);
		
		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, l0Domian);
		PrimitiveTask east = new PrimitiveTask(aEast, l0Domian);
		PrimitiveTask south = new PrimitiveTask(aSouth, l0Domian);
		PrimitiveTask wast = new PrimitiveTask(aWest, l0Domian);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, l0Domian);
		PrimitiveTask dropoff = new PrimitiveTask(aDropoff, l0Domian);
		
		Task[] navTasks = new Task[]{north, east, south, wast};
		Task[] pickupL1Tasks = new Task[]{pickup};
		Task[] dropoffTasks = new Task[]{dropoff};
		
		nav
	}
}
