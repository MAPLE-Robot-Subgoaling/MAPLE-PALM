package taxi.ramdp;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.framework.GroundedTask;
import ramdp.framework.RAMDPLearningAgent;
import ramdp.framework.Task;
import taxi.TaxiDomain;
import taxi.TaxiVisualizer;
import taxi.state.TaxiState;
import utilities.SimpleHashableStateFactory;

public class RAMDPTest {

	public static void runEpisodes(int numEpisode, int maxSteps, Task root, State initial, OOSADomain groundDomain,
			int threshold, double discount, double rmax){
		List<Episode> episodes = new ArrayList<Episode>();
		GroundedTask rootgt = root.getAllGroundedTasks(initial).get(0);
		
		RAMDPLearningAgent ramdp = new RAMDPLearningAgent(rootgt, threshold, discount, rmax, 
				new SimpleHashableStateFactory());
		
		SimulatedEnvironment env = new SimulatedEnvironment(groundDomain, initial);
		VisualActionObserver obs = new VisualActionObserver(groundDomain, TaxiVisualizer.getVisualizer(5, 5));
        obs.initGUI();
        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
        env.addObservers(obs);
		
		for(int i = 1; i <= numEpisode; i++){
			long time = System.currentTimeMillis();
			Episode e = ramdp.runLearningEpisode(env, maxSteps);
			time = System.currentTimeMillis() - time;
			episodes.add(e);
			System.out.println("Episode " + i + " time " + (double)time/1000 );
			env.resetEnvironment();
		}
		
		EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
				(TaxiVisualizer.getVisualizer(5, 5), groundDomain, episodes);
		ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
		ev.initGUI();
	}
	
	public static void main(String[] args) {
		TaxiState s = TaxiDomain.getSmallClassicState(false);
		Task root = TaxiHierarchy.createHierarchy(s, false);
		OOSADomain base = TaxiHierarchy.getGroundDomain();
		
		runEpisodes(10, 10000, root, s, base, 5, 0.99, 30);;
	}
}
