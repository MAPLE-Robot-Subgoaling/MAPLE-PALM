package hiergen.CAT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class SubCAT extends CATrajectory {
    private List<Object> relVars;
    CATrajectory c;

    public SubCAT()
    {
        start = 1000000;
        end = -1;
        lastAction = end;
        actionInds  = new ArrayList<>();
        relVars = new ArrayList<>();
        c = null;
    }

    public SubCAT(int s, int e, List<Integer> actionInds, List<Object> relVars, CATrajectory CAT)
    {
        start = s;
        end = e;
        if(start == 0 && end == 0)
            System.out.println("why???");
        lastAction = end;
        this.relVars = relVars;
        this.actionInds = actionInds;
        this.actions = CAT.actions;
        c = CAT;
        sub = this;
        checkedVariables = c.checkedVariables;
        changedVariables = c.changedVariables;
        baseTrajectory = c.baseTrajectory;
        edges = c.edges;
    }

    public int getStart()
    {
        return start;
    }

    public CATrajectory getCAT() {
        return c;
    }

    public int getEnd()
    {
        return end;
    }
    public List<Integer> getActionInds(){ return actionInds; };

    public SubCAT Unify(SubCAT a)
    {
        System.out.println("Unity");
        //System.out.println(a.CAT);
        SubCAT unification = new SubCAT(a.start, a.end, a.actionInds, relVars, c);

        for(Integer i:this.actionInds)
        {
            if(!unification.actionInds.contains(i))
                unification.actionInds.add(i);
        }

        if(this.start < a.start)
            unification.start = a.start;
        if(this.end > a.end)
            unification.end = a.end;

        return unification;
    }

    public CATrajectory getUltimateActions()
    {
        SubCAT ultimate = this;
        ArrayList<Integer> inds = new ArrayList<>();
        inds.add(end);
        ultimate.actionInds = inds;
        ultimate.start = end;
        return ultimate;
    }

    public List<Object> getRelVars()
    {
        return relVars;
    }

    public void setRelVars(List<Object> relVars)
    {
        this.relVars = relVars;
    }
}
