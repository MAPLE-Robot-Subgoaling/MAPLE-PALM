package hiergen;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.action.Action;

import java.util.ArrayList;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class Task {
    StateConditionTest goal;
    ArrayList<Action> actions;
    ArrayList<String> variables;

    public Task()
    {
        goal = null;
        actions = new ArrayList<Action>();
        variables = new ArrayList<String>();
    }

    public Task(StateConditionTest g, ArrayList<Action> a, ArrayList<String> v)
    {
        goal = g;
        actions = a;
        variables = v;
    }
}
