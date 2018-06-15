package liftCopter;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.visualizer.OOStatePainter;
import burlap.visualizer.ObjectPainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;
import liftCopter.state.*;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.*;

import static liftCopter.LiftCopterConstants.*;

public class LiftCopterVisualizer {
    //this code creates painter and a visualizer for the base taxi domain

    private static Map<String, Color> colors;
    private static int cellsWide, cellsTall;

    private static void initColors(){
        colors = new HashMap<String, Color>();
        colors.put(COLOR_RED, Color.red);
        colors.put(COLOR_YELLOW, Color.YELLOW);
        colors.put(COLOR_BLUE, Color.BLUE);
        colors.put(COLOR_GREEN, Color.GREEN);
        colors.put(COLOR_MAGENTA, Color.MAGENTA);
        colors.put(COLOR_BLACK, Color.BLACK);
        colors.put(COLOR_GRAY, Color.DARK_GRAY);
    }

    public static Visualizer getVisualizer(int w, int h){
        initColors();
        Visualizer v = new Visualizer(getStateRenderLayer(w, h));
        return v;
    }

    public static StateRenderLayer getStateRenderLayer(int w, int h){
        StateRenderLayer rl = new StateRenderLayer();
        OOStatePainter oopainter = new OOStatePainter();

        cellsWide = w;
        cellsTall = h;

        oopainter.addObjectClassPainter(CLASS_LOCATION, new LocationPainter());
        oopainter.addObjectClassPainter(CLASS_AGENT, new CopterPainter());
        oopainter.addObjectClassPainter(CLASS_CARGO, new PassengerPainter());
        oopainter.addObjectClassPainter(CLASS_WALL, new WallPainter());

        rl.addStatePainter(oopainter);

        return rl;
    }

    //these classes add graphics for each of the state objectsS
    public static class CopterPainter implements ObjectPainter{

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            g2.setColor(Color.GRAY);

            float taxiWidth = (float) cWidth / cellsWide;
            float taxiHeight = (float) cHeight / cellsTall;

            LiftCopterAgent copter = (LiftCopterAgent) ob;
            double tx = (double) copter.get(ATT_X);
            double ty = (double) copter.get(ATT_Y);
            float taxix = (float)tx * taxiWidth - cellsWide;
            float taxiy = (float)(cHeight - (.5 + ty) * taxiHeight);

            float scale = 0.5f;
            float realWidth = taxiWidth * scale;
            float realHeight = taxiHeight * scale;
            float realX = taxix;
            float realy = taxiy;
            g2.fillRect((int)realX, (int)realy, (int)realWidth, (int)realHeight);
//            g2.fill(new Ellipse2D.Float(realX, realy, realWidth, realHeight));
        }
    }

    public static class PassengerPainter implements ObjectPainter{
        static int numPassengers = 0;
        ArrayList<String> passengerNumbers = new ArrayList<String>();
        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            LiftCopterState state = (LiftCopterState) s;
            LiftCopterCargo p = (LiftCopterCargo) ob;
            String goalLoc = (String) state.object(p.name()).get(ATT_GOAL_LOCATION);
            String color = (String) state.object(goalLoc).get(ATT_COLOR);
            Color col = colors.get(color);

            g2.setColor(col);

            float passWidth = (float) cWidth / cellsWide;
            float passHeight = (float) cHeight / cellsTall;

            double px = (double) p.get(ATT_X);
            double py = (double) p.get(ATT_Y);
            float passx = (float)(px * passWidth);
            float passy = (float)(cHeight - (1 + py) * passHeight);
            passx = passx + passWidth / 2f;
            passy = passy + passHeight / 2f;

            float scale = 0.7f;

            boolean inTaxi = (boolean) p.get(ATT_PICKED_UP);
            if(inTaxi){
                scale = 0.5f;
                if(!passengerNumbers.contains(p.name()) ){
                    ///encounter passenger not in the set but in the taxi, add to list
                    passengerNumbers.add(p.name());
                    numPassengers++;
                }
            }else{
                if(passengerNumbers.contains(p.name()) ){
                    ///encounter passenger in the set but not in the taxi, remove from list
                    passengerNumbers.remove(p.name());
                    numPassengers--;
                }
            }


            float realWidth = passWidth * scale;
            float realHeight = passHeight * scale;
            float realX = passx - (realWidth / 2f);
            float realy = passy - (realHeight / 2f);
            if(inTaxi){
                float start = 90+(360/numPassengers)*passengerNumbers.indexOf(p.name());
                float extent = 360/numPassengers;
                g2.fill(new Arc2D.Float(realX, realy, realWidth, realHeight, start, extent, Arc2D.PIE));
            }else{
                g2.fill(new Ellipse2D.Float(realX, realy, realWidth, realHeight));
            }

        }
    }

    public static class LocationPainter implements ObjectPainter{

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            LiftCopterLocation l = (LiftCopterLocation) ob;
            String color = (String) l.get(ATT_COLOR);
            Color col = colors.get(color).darker();

            g2.setColor(col);

            float locWidth = (float) cWidth / cellsWide;
            float locHeight = (float) cHeight / cellsTall;

            double lx = (double) l.get(ATT_X);
            double ly = (double) l.get(ATT_Y);
            double lh = (double) l.get(ATT_H);
            double lw = (double) l.get(ATT_W);
            double locx = (lx - lw/2)* locWidth;
            double locy = cHeight - (ly + lh/2) * locHeight;

            g2.fillRect((int)locx, (int)locy, (int)locWidth, (int)locHeight);
        }
    }

    public static class WallPainter implements ObjectPainter{

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(10));

            float wallWidth = (float) cWidth / cellsWide;
            float wallHeight = (float) cHeight / cellsTall;

            LiftCopterWall w = (LiftCopterWall) ob;
            float wh, ww;
            ww = (float)((double) w.get(ATT_WIDTH) * wallWidth);
            wh = (float)((double) w.get(ATT_HEIGHT) * wallHeight);
            double startx = (double) w.get(ATT_START_X);
            double starty = (double) w.get(ATT_START_Y);
            float wx1 = (float)startx * wallWidth;
            float wy1 = (float)( cHeight - (starty * wallHeight)-wh);


            g2.fillRect((int) wx1, (int) wy1, (int)ww, (int)wh );
        }
    }
}
