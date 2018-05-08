package palm.rmax.agent;

import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import palm.agent.ExpertPALMModel;
import palm.agent.PALMModel;
import palm.agent.PALMModelGenerator;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;

public class ExpertNavModelGenerator implements PALMModelGenerator {

    private int threshold;
    private double rmax;
    private double gamma;
    private boolean useMultitimeModel;
    private HashableStateFactory hashingFactory;

    public ExpertNavModelGenerator( int threshold, double rmax, HashableStateFactory hs,
                                   double gamma, boolean useMultitimeModel) {
        this.threshold = threshold;
        this.rmax = rmax;
        this.hashingFactory = hs;
        this.gamma = gamma;
        this.useMultitimeModel = useMultitimeModel;
    }

    @Override
    public PALMModel getModelForTask(GroundedTask t) {
        if (t.getAction().actionName().startsWith(TaxiGetDomain.ACTION_NAV)) {
            ObjectParameterizedAction nav_action = (ObjectParameterizedAction) t.getAction();
            String[] params = nav_action.getObjectParameters();
            String locName = params[0];
            TaxiNavDomain nav_gen = new TaxiNavDomain(locName);
            OODomain nav_domain = nav_gen.generateDomain();
            FactoredModel nav_model = (FactoredModel) ((OOSADomain) nav_domain).getModel();
            PALMModel nav_palm_model = new ExpertPALMModel(nav_model);
            PALMModel nav = new ExpertPALMModel(nav_palm_model);
            return  nav;
        }else{
            return new HierarchicalRmaxModel(t, this.threshold, this.rmax,
                    this.hashingFactory, this.gamma, this.useMultitimeModel);
        }
    }
}
