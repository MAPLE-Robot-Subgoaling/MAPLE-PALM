package testing;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchies.structureLearning.CATrajectory;
import taxi.Taxi;
import taxi.TaxiDBNParents;
import taxi.stateGenerator.TaxiStateFactory;
import utilities.ValueIteration;

public class CATTest {

	public static void main(String[] args) {
		double gamma = 0.9;
		HashableStateFactory hs = new SimpleHashableStateFactory();
		double maxDelta = 0.01;
		int maxIterations = 1000;
		State init = TaxiStateFactory.createSmallState();
		Taxi tGen = new Taxi();
		OOSADomain taxiDomain = tGen.generateDomain(); 
		FullModel model = (FullModel) taxiDomain.getModel();
		
		ValueIteration vi = new ValueIteration(taxiDomain, gamma, hs, maxDelta, maxIterations);
		Policy p = vi.planFromState(init);
		Episode e = PolicyUtils.rollout(p, init, model);
		
		CATrajectory cat = new CATrajectory();
		cat.annotateTrajectory(e, TaxiDBNParents.getParents(init), model);
		System.out.println(cat);
		

	}

}
