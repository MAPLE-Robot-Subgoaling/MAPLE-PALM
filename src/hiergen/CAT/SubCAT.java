package hiergen.CAT;

import java.util.List;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class SubCAT {
    private int start, end;
    private List<Integer> actions;
    public SubCAT(int s, int e, List<Integer> actions)
    {
        start = s;
        end = e;
        this.actions = actions;
    }

    public int getStart()
    {
        return start;
    }
    public int getEnd()
    {
        return end;
    }
    public List<Integer> getActionInds(){ return actions; };
}
