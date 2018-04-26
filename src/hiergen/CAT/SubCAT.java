package hiergen.CAT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class SubCAT {
    private int start, end;
    private List<Integer> actions;
    private CATrajectory CAT;

    public SubCAT()
    {
        start = 1000000;
        end = -1;
        actions  = new ArrayList<>();
        CAT = null;
    }

    public SubCAT(int s, int e, List<Integer> actions, CATrajectory CAT)
    {
        start = s;
        end = e;
        this.actions = actions;
        this.CAT = CAT;
    }

    public int getStart()
    {
        return start;
    }

    public CATrajectory getCAT() {
        return CAT;
    }

    public int getEnd()
    {
        return end;
    }
    public List<Integer> getActionInds(){ return actions; };

    public static SubCAT Unify(SubCAT a, SubCAT b)
    {
        SubCAT unification = new SubCAT(a.start, a.end, a.actions, b.CAT);

        for(Integer i: b.actions)
            unification.actions.add(i);
        if(b.start < a.start)
            unification.start = b.start;
        if(b.end > a.end)
            unification.end = b.end;

        return unification;
    }
}
