package taxi;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.visualizer.OOStatePainter;
import burlap.visualizer.ObjectPainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;
import taxi.state.TaxiWall;

public class TaxiVisualizer {
	//this code creates painter and a visualizer for the base taxi domain
	
	private static Map<String, Color> colors;
	private static int cellsWide, cellsTall;
	
	private static void initColors(){
		colors = new HashMap<String, Color>();
		colors.put(Taxi.COLOR_RED, Color.red);
		colors.put(Taxi.COLOR_YELLOW, Color.YELLOW);
		colors.put(Taxi.COLOR_BLUE, Color.BLUE);
		colors.put(Taxi.COLOR_GREEN, Color.GREEN);
		colors.put(Taxi.COLOR_MAGENTA, Color.MAGENTA);
		colors.put(Taxi.COLOR_BLACK, Color.BLACK);
		colors.put(Taxi.COLOR_GRAY, Color.DARK_GRAY);
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
        
        oopainter.addObjectClassPainter(Taxi.CLASS_LOCATION, new LocationPainter());
        oopainter.addObjectClassPainter(Taxi.CLASS_TAXI, new TaxiPainter());
        oopainter.addObjectClassPainter(Taxi.CLASS_PASSENGER, new PassengerPainter());
        oopainter.addObjectClassPainter(Taxi.CLASS_WALL, new WallPainter());

        rl.addStatePainter(oopainter);
        
        return rl;
	}
	
	//these classes add graphics for each of the state objectsS
	public static class TaxiPainter implements ObjectPainter{

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
			g2.setColor(Color.GRAY);
			
			float taxiWidth = (float) cWidth / cellsWide;
			float taxiHeight = (float) cHeight / cellsTall;
			
			TaxiAgent taxi = (TaxiAgent) ob;
			int tx = (int) taxi.get(Taxi.ATT_X);
			int ty = (int) taxi.get(Taxi.ATT_Y);
			float taxix = tx * taxiWidth - cellsWide;
			float taxiy = cHeight - (1 + ty) * taxiHeight;
			
			float scale = 0.9f;
			float realWidth = taxiWidth * scale;
			float realHeight = taxiHeight * scale;
			float realX = taxix + 0.08f * taxiWidth;
			float realy = taxiy + 0.05f * taxiHeight;
			
			g2.fill(new Ellipse2D.Float(realX, realy, realWidth, realHeight));
		}
	}
	
	public static class PassengerPainter implements ObjectPainter{

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
			TaxiState state = (TaxiState) s;
			TaxiPassenger p = (TaxiPassenger) ob;
			String goalLoc = (String) state.getPassengerAtt(p.name(), Taxi.ATT_GOAL_LOCATION);
			String color = (String) state.getLocationAtt(goalLoc, Taxi.ATT_COLOR);
			Color col = colors.get(color);
			
			g2.setColor(col);
	
			float passWidth = (float) cWidth / cellsWide;
			float passHeight = (float) cHeight / cellsTall;
			
			int px = (int) p.get(Taxi.ATT_X);
			int py = (int) p.get(Taxi.ATT_Y);
			float passx = px * passWidth;
			float passy = cHeight - (1 + py) * passHeight;
			passx = passx + passWidth / 2f;
			passy = passy + passHeight / 2f;
			
			float scale = 0.7f;
			
			boolean inTaxi = (boolean) p.get(Taxi.ATT_IN_TAXI);
			if(inTaxi)
				scale = 0.5f;
			
			float realWidth = passWidth * scale;
			float realHeight = passHeight * scale;
			float realX = passx - (realWidth / 2f);
			float realy = passy - (realHeight / 2f);
			
			g2.fill(new Ellipse2D.Float(realX, realy, realWidth, realHeight));
		}
	}
	
	public static class LocationPainter implements ObjectPainter{

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
			TaxiLocation l = (TaxiLocation) ob;
			String color = (String) l.get(Taxi.ATT_COLOR);
			Color col = colors.get(color).darker();
			
			g2.setColor(col);
			
			float locWidth = (float) cWidth / cellsWide;
			float locHeight = (float) cHeight / cellsTall;
			
			int lx = (int) l.get(Taxi.ATT_X);
			int ly = (int) l.get(Taxi.ATT_Y);
			float locx = lx * locWidth;
			float locy = cHeight - (1 + ly) * locHeight;
			
			g2.fill(new Ellipse2D.Float(locx, locy, locWidth, locHeight));
		}
	}
	
	public static class WallPainter implements ObjectPainter{

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(10));
			
			float wallWidth = (float) cWidth / cellsWide;
			float wallHeight = (float) cHeight / cellsTall;
			
			TaxiWall w = (TaxiWall) ob;
			int startx = (int) w.get(Taxi.ATT_START_X);
			int starty = (int) w.get(Taxi.ATT_START_Y);
			float wx1 = startx * wallWidth;
			float wy1 = cHeight - starty * wallHeight;
			float wx2, wy2;
			
			int length = (int) w.get(Taxi.ATT_LENGTH);
			boolean isHorizontal = (boolean) w.get(Taxi.ATT_IS_HORIZONTAL);
			if(isHorizontal){
				wx2 = wx1 + length * wallWidth;
				wy2 = wy1;
			}else{
				wx2 = wx1;
				wy2 = wy1 - length * wallHeight;
			}
			
			g2.drawLine((int) wx1, (int) wy1, (int) wx2, (int) wy2);
		}
	}
}
