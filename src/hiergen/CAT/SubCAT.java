package hiergen.CAT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class SubCAT extends CATrajectory {
    private int start, end;
    private List<Integer> actionInds;

    public SubCAT()
    {
        start = 1000000;
        end = -1;
        actionInds  = new ArrayList<>();
    }

    public SubCAT(int s, int e, List<Integer> actionInds, CATrajectory CAT)
    {
        start = s;
        end = e;
        this.actionInds = actionInds;
        this.actions = CAT.actions;
    }

    public int getStart()
    {
        return start;
    }

    public CATrajectory getCAT() {
        return this;
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
        SubCAT unification = new SubCAT(a.start, a.end, a.actionInds, this);

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

    /*public void Merge(SubCAT s)
    {
        int min = 1000000, max = -1;
        ArrayList<Integer> newInds = new ArrayList<>();
        for(Integer i: s.getActionInds())
        {
            if(actions.contains(i))
            {
                newInds.add(i);
                if(i < min)
                    min = i;
                if(i > max)
                    max = i;
            }

        }

        start = min;
        end = max;
    }*/
}
