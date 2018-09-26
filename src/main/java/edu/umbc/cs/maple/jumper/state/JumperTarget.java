package edu.umbc.cs.maple.jumper.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.utilities.MutableObject;
import edu.umbc.cs.maple.utilities.MutableObjectInstance;

import java.util.List;

import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_X;
import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_Y;
import static edu.umbc.cs.maple.jumper.JumperConstants.CLASS_TARGET;

public class JumperTarget extends JumperPoint {

    public JumperTarget() {
        // for de/serialization
    }

    public JumperTarget(String name, JumperTarget source) {
        super();
        this.name = name;
        mirror(source);
    }

    public JumperTarget(String name, double x, double y) {
        super(name, x, y);
    }

    @Override
    public String className() {
        return CLASS_TARGET;
    }

    @Override
    public ObjectInstance copyWithName(String name) {
        return new JumperTarget(name, this);
    }

}
