package action.models;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;
import utilities.ValueIteration;

public class TrajectoryGengerator {

	public static List<Episode> generateTrajectories(StateGenerator stateGen, int numTrajectories,
			 SADomain domain, double gamma, HashableStateFactory hashingFactory, Double maxDelta, int maxIterations){
		
		ValueIteration vi = new ValueIteration(domain, gamma, hashingFactory, maxDelta, maxIterations);
		
		List<Episode> episodes = new ArrayList<Episode>();
		for(int i = 0; i < numTrajectories; i++){
			State init = stateGen.generateState();
			Policy p = vi.planFromState(init);
			Episode e = PolicyUtils.rollout(p, init, domain.getModel());
			episodes.add(e);
		}
		
		return episodes;
	}
}
