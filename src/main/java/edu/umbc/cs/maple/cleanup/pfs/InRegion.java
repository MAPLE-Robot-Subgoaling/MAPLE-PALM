package edu.umbc.cs.maple.cleanup.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class InRegion extends PropositionalFunction {

    protected boolean countBoundary;

    public InRegion(String name, String[] parameterClasses, boolean countBoundary) {
        super(name, parameterClasses);
        this.countBoundary = countBoundary;
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        CleanupState cws = (CleanupState) s;
        ObjectInstance o = cws.object(params[0]);
        ObjectInstance region = cws.object(params[1]);

        if (o == null) { return false; }

        String abstractInRegion = (String) o.get(Cleanup.ATT_REGION);
        if (abstractInRegion != null) {
            // this object is abstract, as in the Cleanup AMDP
            return abstractInRegion.equals(region.name());
        }

        if (o == null || region == null) {
            return false;
        }
        int x = (Integer) o.get(Cleanup.ATT_X);
        int y = (Integer) o.get(Cleanup.ATT_Y);
        return CleanupState.regionContainsPoint(region, x, y, countBoundary);
    }
}
