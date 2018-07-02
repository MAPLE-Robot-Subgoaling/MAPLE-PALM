package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.ExpertPALMModel;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.TaxiNavDomain;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ACTION_NAV;

public class ExpertNavModelGenerator implements PALMModelGenerator {

    private int threshold;
    private double rmax;
    private double gamma;
    private boolean useMultitimeModel;
    private HashableStateFactory hashingFactory;

    public ExpertNavModelGenerator(HashableStateFactory hsf, double gamma, int threshold, double rmax, boolean useMultitimeModel) {
        this.hashingFactory = hsf;
        this.gamma = gamma;
        this.threshold = threshold;
        this.rmax = rmax;
        this.useMultitimeModel = useMultitimeModel;
    }

    public ExpertNavModelGenerator(HashableStateFactory hsf, ExperimentConfig config) {
        this(hsf, config.gamma, config.rmax.threshold, config.rmax.vmax, config.rmax.use_multitime_model);
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        if (t.getAction().actionName().startsWith(ACTION_NAV)) {
            ObjectParameterizedAction nav_action = (ObjectParameterizedAction) t.getAction();
            String[] params = nav_action.getObjectParameters();
            String locName = params[0];
            TaxiNavDomain nav_gen = new TaxiNavDomain(locName);
            OODomain nav_domain = nav_gen.generateDomain();
            FactoredModel nav_model = (FactoredModel) ((OOSADomain) nav_domain).getModel();
//            PALMModel nav_palm_model = new ExpertPALMModel(nav_model);
            PALMModel nav = new ExpertPALMModel(nav_model, gamma);
            return  nav;
        }else{
            return new HierarchicalRmaxModel(t.getTask(), this.threshold, this.rmax,
                    this.hashingFactory, this.gamma, this.useMultitimeModel);
        }
    }
}
