package edu.umbc.cs.maple.jumper.state;

import burlap.mdp.core.oo.state.ObjectInstance;

import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_X;
import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_Y;
import static edu.umbc.cs.maple.jumper.JumperConstants.CLASS_AGENT;

public class JumperAgent extends JumperPoint {

    public JumperAgent() {
        // for de/serialization
    }

    public JumperAgent(String name, JumperAgent source) {
        this.name = name;
        this.mirror(source);
    }

    public JumperAgent(String name, double x, double y) {
        super(name, x, y);
    }

    @Override
    public String className() {
        return CLASS_AGENT;
    }

    @Override
    public ObjectInstance copyWithName(String name) {
        return new JumperAgent(name, this);
    }

}
