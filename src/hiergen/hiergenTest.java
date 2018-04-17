package hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hiergen.CAT.CATrajectory;
import hiergen.CAT.CreateActionModels;
import hiergen.CAT.TrajectoryGenerator;
import hiergen.CAT.VariableTree;
import taxi.Taxi;
import taxi.TaxiVisualizer;
import taxi.stateGenerator.RandomPassengerTaxiState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class hiergenTest {
    public static void main(String [] args){
        Taxi test = new Taxi();
        int numTrajectories = 20;
        double gamma = 0.95;
        OOSADomain dom = test.generateDomain();
        List<Episode> episodes = TrajectoryGenerator.generateQLearnedTrajectories(new RandomPassengerTaxiState(), numTrajectories, dom, gamma, new SimpleHashableStateFactory());
        /*
        EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(5, 5),
                dom, episodes);
        v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
        v.initGUI();
        */
        Action a = episodes.get(0).actionSequence.get(0);
        ArrayList<CATrajectory> CATs = new ArrayList<CATrajectory>();
        Map<String, Map<String, VariableTree>> actionModels = CreateActionModels.createModels(episodes);
        /*System.out.println(a);
        System.out.println(actionModels.get(a.toString()).get("R"));
        System.out.println("\n\n\n\n\n\n\n");
        System.out.println(actionModels);*/
        for(Episode e:episodes)
        {
            CATrajectory temp = new CATrajectory();
            temp.annotateTrajectory(e, actionModels, (FullModel) dom.getModel());
            System.out.println(temp);
            CATs.add(temp);
        }


    }
}
