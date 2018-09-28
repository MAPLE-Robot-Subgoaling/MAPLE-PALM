package edu.umbc.cs.maple.jumper;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.visualizer.OOStatePainter;
import burlap.visualizer.ObjectPainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.jumper.state.JumperAgent;
import edu.umbc.cs.maple.jumper.state.JumperPoint;
import edu.umbc.cs.maple.jumper.state.JumperState;
import edu.umbc.cs.maple.jumper.state.JumperTarget;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import static edu.umbc.cs.maple.jumper.JumperConstants.*;

public class JumperVisualizer {

    private static float width;
    private static float height;
    private static float goalRadius;

    public static Visualizer getVisualizer(float w, float h, float goalRadius){
        Visualizer v = new Visualizer(getStateRenderLayer(w, h, goalRadius));
        return v;
    }

    public static StateRenderLayer getStateRenderLayer(float w, float h, float goalRadius){
        StateRenderLayer rl = new StateRenderLayer();
        OOStatePainter oopainter = new OOStatePainter();

        width = w;
        height = h;
        JumperVisualizer.goalRadius = goalRadius;

        oopainter.addObjectClassPainter(CLASS_AGENT, new JumperVisualizer.EllipsePainter(Color.BLUE));
        oopainter.addObjectClassPainter(CLASS_TARGET, new JumperVisualizer.EllipsePainter(Color.RED));

        rl.addStatePainter(oopainter);

        return rl;
    }

    //these classes add graphics for each of the state objectsS
    public static class EllipsePainter implements ObjectPainter {

        private Color color;

        public EllipsePainter(Color color) {
            this.color = color;
        }

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            g2.setColor(color);

            JumperPoint p = (JumperPoint) ob;
            float x = (float) (double) p.get(ATT_X);
            float y = (float) (double) p.get(ATT_Y);
            float w = cWidth / 10f;
            float h = cHeight / 10f;
            if (p instanceof JumperTarget) {
                w = 2*goalRadius * cWidth;
                h = 2*goalRadius * cHeight;
            }
            x *= cWidth;
            y *= cHeight;
            y = cHeight - y;
            y -= h * 0.5f;
            x -= w * 0.5f;
            g2.fill(new Ellipse2D.Float(x, y, w, h));
        }
    }


}
