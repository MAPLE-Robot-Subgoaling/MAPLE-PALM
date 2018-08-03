package edu.umbc.cs.maple.cleanup.state;

import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.CleanupGoalDescription;
import edu.umbc.cs.maple.utilities.BurlapConstants;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class CleanupRandomStateGenerator implements StateGenerator {

    private static final int DEBUG_CODE = 932891293;
    private int numBlocks = 2;
    private int minX = 0;
    private int minY = 0;
    private int maxX = 0;
    private int maxY = 0;

    public CleanupRandomStateGenerator(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public CleanupRandomStateGenerator(Cleanup cleanup) {
        this.minX = cleanup.getMinX();
        this.minY = cleanup.getMinY();
        this.maxX = cleanup.getMaxX();
        this.maxY = cleanup.getMaxY();
    }

    public static void setDebugMode(boolean mode) {
        DPrint.toggleCode(DEBUG_CODE, mode);
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

    public State generateTaxiInCleanup(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 1;
        int numDoors = 4;

        int mx = getWidth() / 2;
        int my = getHeight() / 2;

        int ax = mx;
        int ay = my;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);

        List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS_BLOCKS);
        colors.add("green");
        colors.add("blue");
        colors.add("yellow");
        colors.add("red");

        int mainW = 3;
        int mainH = 3;

        int index = 0;
        do {
            int bx = ax + (rng.nextBoolean() ? -1 : 1);
            int by = ay + (rng.nextBoolean() ? -1 : 1);
            if (!s.blockAt(bx, by)) {
                s.addObject(new CleanupBlock("block" + index, bx, by, "backpack", colors.get(rng.nextInt(numDoors - 1))));
                numBlocks -= 1;
                index += 1;
            }
        } while (numBlocks > 0);


        s.addObject(new CleanupRoom("room0", mx - mainW, mx + mainW, my - mainH, my + mainH, "cyan", Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupDoor("door0", mx - mainW + 1, mx - mainW + 1, my, my, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(0)));
        s.addObject(new CleanupDoor("door1", mx + mainW - 1, mx + mainW - 1, my, my, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(1)));
        s.addObject(new CleanupDoor("door2", mx, mx, my - mainH + 1, my - mainH + 1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(2)));
        s.addObject(new CleanupDoor("door3", mx, mx, my + mainH - 1, my + mainH - 1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(3)));
        mx -= mainW + 1;
//		s.addObject(new CleanupRoom("room1", mx-1, mx+1, my-1, my+1, colors.get(0), Cleanup.SHAPE_ROOM));
        mx += (mainW + 1) * 2;
//		s.addObject(new CleanupRoom("room2", mx-1, mx+1, my-1, my+1, colors.get(1), Cleanup.SHAPE_ROOM));
        mx -= mainW + 1;
        my -= mainH + 1;
//		s.addObject(new CleanupRoom("room3", mx-1, mx+1, my-1, my+1, colors.get(2), Cleanup.SHAPE_ROOM));
        my += (mainH + 1) * 2;
//		s.addObject(new CleanupRoom("room4", mx-1, mx+1, my-1, my+1, colors.get(3), Cleanup.SHAPE_ROOM));

//		int i = 0;
//		while (i < numBlocks) {
//			String id = "block"+i;
//			CleanupRoom room = (CleanupRoom) s.objectsOfClass(Cleanup.CLASS_ROOM).get(rng.nextInt(numRooms));
//			int rLeft = ((Integer) room.get(Cleanup.ATT_LEFT))+1;
//			int rRight = ((Integer) room.get(Cleanup.ATT_RIGHT))-1;
//			int rTop = ((Integer) room.get(Cleanup.ATT_TOP))-1;
//			int rBottom = ((Integer) room.get(Cleanup.ATT_BOTTOM))+1;
//			int bX = rng.nextInt(rRight - rLeft) + rLeft;
//			int bY = rng.nextInt(rTop - rBottom) + rBottom;
//			if (s.isOpen(room, bX, bY)) {
//				String shape = Cleanup.SHAPES[rng.nextInt(Cleanup.SHAPES.length)];
//				String color = Cleanup.COLORS_BLOCKS[rng.nextInt(Cleanup.COLORS_BLOCKS.length)];
//				DPrint.cl(DEBUG_CODE,"block"+i+": "+ shape + " " + color + " (" + bX + ", " + bY + ") in the " + room.get(Cleanup.ATT_COLOR) + " " + ((CleanupRoom)room).name);
//				s.addObject(new CleanupBlock(id, bX, bY, shape, color));
//				i = i + 1;
//			}
//		}

        return s;
    }

    public State generateCentralRoomWithClosetsAndBeacon(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 5;
        int numDoors = 5;

        int mx = getWidth() / 2;
        int my = getHeight() / 2;

        int ax = mx;
        int ay = my;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);

        List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS_BLOCKS);
        colors.add("green");
        colors.add("blue");
        colors.add("yellow");
        colors.add("red");

        int mainW = 2;
        int mainH = 2;

        int index = 0;
        String lastBlockColor = null;
        do {
            int bx = ax + (rng.nextBoolean() ? -1 : 1);
            int by = ay + (rng.nextBoolean() ? -1 : 1);
            if (!s.blockAt(bx, by)) {
                lastBlockColor = colors.get(rng.nextInt(numRooms - 1));
                s.addObject(new CleanupBlock("block" + index, bx, by, "backpack", lastBlockColor));
                numBlocks -= 1;
                index += 1;
            }
        } while (numBlocks > 0);

        s.addObject(new CleanupRoom("room0", mx - mainW, mx + mainW, my - mainH, my + mainH, "cyan", Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupDoor("door0", mx - mainW, mx - mainW, my, my, Cleanup.LOCKABLE_STATES[0], colors.get(0)));
        s.addObject(new CleanupDoor("door1", mx + mainW, mx + mainW, my, my, Cleanup.LOCKABLE_STATES[0], colors.get(1)));
        s.addObject(new CleanupDoor("door2", mx, mx, my - mainH, my - mainH, Cleanup.LOCKABLE_STATES[0], colors.get(2)));
        s.addObject(new CleanupDoor("door3", mx, mx, my + mainH, my + mainH, Cleanup.LOCKABLE_STATES[0], colors.get(3)));
        s.addObject(new CleanupDoor("beacon", mx, mx, my, my, Cleanup.LOCKABLE_STATES[0], "beacon", lastBlockColor));
        mx -= mainW + 1;
        s.addObject(new CleanupRoom("room1", mx - 1, mx + 1, my - 1, my + 1, colors.get(0), Cleanup.SHAPE_ROOM));
        mx += (mainW + 1) * 2;
        s.addObject(new CleanupRoom("room2", mx - 1, mx + 1, my - 1, my + 1, colors.get(1), Cleanup.SHAPE_ROOM));
        mx -= mainW + 1;
        my -= mainH + 1;
        s.addObject(new CleanupRoom("room3", mx - 1, mx + 1, my - 1, my + 1, colors.get(2), Cleanup.SHAPE_ROOM));
        my += (mainH + 1) * 2;
        s.addObject(new CleanupRoom("room4", mx - 1, mx + 1, my - 1, my + 1, colors.get(3), Cleanup.SHAPE_ROOM));

        return s;
    }

    public State generateCentralRoomWithFourDoors(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 1;
        int numDoors = 4;

        int mx = getWidth() / 2;
        int my = getHeight() / 2;

        int ax = mx;
        int ay = my;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);

        List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS_BLOCKS);
        colors.add("green");
        colors.add("blue");
        colors.add("yellow");
        colors.add("red");

        List<String> shapes = Arrays.asList(Cleanup.SHAPES);

        int mainW = 2 + rng.nextInt(3); // 4
        int mainH = 2 + rng.nextInt(3); // 4;

        int index = 0;
        while (numBlocks > 0) {
            int bx = ax + (rng.nextBoolean() ? -1 : 1);
            int by = ay + (rng.nextBoolean() ? -1 : 1);
            if (!s.blockAt(bx, by)) {
                String color = "";//colors.get(rng.nextInt(numDoors - 1));
                String shape = "";
                boolean fresh = false;
                while (!fresh) {
                    color = colors.get(rng.nextInt(numDoors - 1));
                    shape = shapes.get(rng.nextInt(shapes.size()));
                    fresh = true;
                    for (CleanupBlock block : s.getBlocks().values()) {
                        if (color.equals(block.get(Cleanup.ATT_COLOR))) {
                            fresh = false;
                            break;
                        }
                    }
                }
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                numBlocks -= 1;
                index += 1;
            }
        }


        // align all the doors in the middle x/y of the room
        int door0y = my;
        int door1y = my;
        int door2x = mx;
        int door3x = mx;
        // offset all the doors randomly along their wall
        door0y += rng.nextInt(mainH) * (rng.nextBoolean() ? 1 : -1);
        door1y += rng.nextInt(mainH) * (rng.nextBoolean() ? 1 : -1);
        door2x += rng.nextInt(mainW) * (rng.nextBoolean() ? 1 : -1);
        door3x += rng.nextInt(mainW) * (rng.nextBoolean() ? 1 : -1);

        String roomColor = Cleanup.COLORS_ROOMS[rng.nextInt(Cleanup.COLORS_ROOMS.length)];
        s.addObject(new CleanupRoom("room0", mx - mainW, mx + mainW, my - mainH, my + mainH, roomColor, Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupDoor("door0", mx - mainW, mx - mainW, door0y, door0y, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door1", mx + mainW, mx + mainW, door1y, door1y, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door2", door2x, door2x, my - mainH, my - mainH, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door3", door3x, door3x, my + mainH, my + mainH, Cleanup.LOCKABLE_STATES[0]));

        return s;
    }


    public State generateTwoRoomsWithFourDoors(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 4;

        int mx = getWidth() / 2;
        int my = getHeight() / 2;

        int ax = mx;
        int ay = my;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);

        List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS_BLOCKS);
        colors.add("green");
        colors.add("blue");
        colors.add("yellow");
        colors.add("red");

        List<String> shapes = Arrays.asList(Cleanup.SHAPES);

        int mainW = 2 + rng.nextInt(3); // 4
        int mainH = 2 + rng.nextInt(3); // 4;

        int index = 0;
        while (numBlocks > 0) {
            int bx = ax + (rng.nextBoolean() ? -1 : 1);
            int by = ay + (rng.nextBoolean() ? -1 : 1);
            if (!s.blockAt(bx, by)) {
                String color = "";//colors.get(rng.nextInt(numDoors - 1));
                String shape = "";
                boolean fresh = false;
                while (!fresh) {
                    color = colors.get(rng.nextInt(numDoors - 1));
                    shape = shapes.get(rng.nextInt(shapes.size()));
                    fresh = true;
                    for (CleanupBlock block : s.getBlocks().values()) {
                        if (color.equals(block.get(Cleanup.ATT_COLOR))) {
                            fresh = false;
                            break;
                        }
                    }
                }
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                numBlocks -= 1;
                index += 1;
            }
        }


        // align all the doors in the middle x/y of the room
        int door0y = my;
        int door1y = my;
        int door2x = mx;
        int door3x = mx;
        // offset all the doors randomly along their wall
        door0y += rng.nextInt(mainH) * (rng.nextBoolean() ? 1 : -1);
        door1y += rng.nextInt(mainH) * (rng.nextBoolean() ? 1 : -1);
        door2x += rng.nextInt(mainW) * (rng.nextBoolean() ? 1 : -1);
        door3x += rng.nextInt(mainW) * (rng.nextBoolean() ? 1 : -1);

        int bigRoomRadiusWidth = mx;
        int bigRoomRadiusHeight = my;
        String bigRoomColor = Cleanup.COLORS_ROOMS[rng.nextInt(Cleanup.COLORS_ROOMS.length)];
        s.addObject(new CleanupRoom("room1", mx - bigRoomRadiusWidth, mx + bigRoomRadiusWidth, my - bigRoomRadiusHeight, my + bigRoomRadiusHeight, bigRoomColor, Cleanup.SHAPE_ROOM));

        String roomColor = Cleanup.COLORS_ROOMS[rng.nextInt(Cleanup.COLORS_ROOMS.length)];
        s.addObject(new CleanupRoom("room0", mx - mainW, mx + mainW, my - mainH, my + mainH, roomColor, Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupDoor("door0", mx - mainW, mx - mainW, door0y, door0y, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door1", mx + mainW, mx + mainW, door1y, door1y, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door2", door2x, door2x, my - mainH, my - mainH, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door3", door3x, door3x, my + mainH, my + mainH, Cleanup.LOCKABLE_STATES[0]));

        return s;
    }

    public State generateCentralRoomWithClosets(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 5;
        int numDoors = 4;

        int mx = getWidth() / 2;
        int my = getHeight() / 2;

        int ax = mx;
        int ay = my;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);

        List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS_BLOCKS);
        colors.add("green");
        colors.add("blue");
        colors.add("yellow");
        colors.add("red");

        int mainW = 2;
        int mainH = 2;

        int index = 0;
        while (numBlocks > 0) {
            int bx = ax + (rng.nextBoolean() ? -1 : 1);
            int by = ay + (rng.nextBoolean() ? -1 : 1);
            if (!s.blockAt(bx, by)) {
                String color = "";//colors.get(rng.nextInt(numDoors - 1));
                boolean fresh = false;
                while (!fresh) {
                    color = colors.get(rng.nextInt(numDoors - 1));
                    fresh = true;
                    for (CleanupBlock block : s.getBlocks().values()) {
                        if (color.equals(block.get(Cleanup.ATT_COLOR))) {
                            fresh = false;
                            break;
                        }
                    }
                }
                s.addObject(new CleanupBlock("block" + index, bx, by, "backpack", color));
                numBlocks -= 1;
                index += 1;
            }
        }
        s.addObject(new CleanupRoom("room0", mx - mainW, mx + mainW, my - mainH, my + mainH, "cyan", Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupDoor("door0", mx - mainW, mx - mainW, my, my, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door1", mx + mainW, mx + mainW, my, my, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door2", mx, mx, my - mainH, my - mainH, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door3", mx, mx, my + mainH, my + mainH, Cleanup.LOCKABLE_STATES[0]));
        mx -= mainW + 1;
        s.addObject(new CleanupRoom("room1", mx - 1, mx + 1, my - 1, my + 1, colors.get(0), Cleanup.SHAPE_ROOM));
        mx += (mainW + 1) * 2;
        s.addObject(new CleanupRoom("room2", mx - 1, mx + 1, my - 1, my + 1, colors.get(1), Cleanup.SHAPE_ROOM));
        mx -= mainW + 1;
        my -= mainH + 1;
        s.addObject(new CleanupRoom("room3", mx - 1, mx + 1, my - 1, my + 1, colors.get(2), Cleanup.SHAPE_ROOM));
        my += (mainH + 1) * 2;
        s.addObject(new CleanupRoom("room4", mx - 1, mx + 1, my - 1, my + 1, colors.get(3), Cleanup.SHAPE_ROOM));

//		int i = 0;
//		while (i < numBlocks) {
//			String id = "block"+i;
//			CleanupRoom room = (CleanupRoom) s.objectsOfClass(Cleanup.CLASS_ROOM).get(rng.nextInt(numRooms));
//			int rLeft = ((Integer) room.get(Cleanup.ATT_LEFT))+1;
//			int rRight = ((Integer) room.get(Cleanup.ATT_RIGHT))-1;
//			int rTop = ((Integer) room.get(Cleanup.ATT_TOP))-1;
//			int rBottom = ((Integer) room.get(Cleanup.ATT_BOTTOM))+1;
//			int bX = rng.nextInt(rRight - rLeft) + rLeft;
//			int bY = rng.nextInt(rTop - rBottom) + rBottom;
//			if (s.isOpen(room, bX, bY)) {
//				String shape = Cleanup.SHAPES[rng.nextInt(Cleanup.SHAPES.length)];
//				String color = Cleanup.COLORS_BLOCKS[rng.nextInt(Cleanup.COLORS_BLOCKS.length)];
//				DPrint.cl(DEBUG_CODE,"block"+i+": "+ shape + " " + color + " (" + bX + ", " + bY + ") in the " + room.get(Cleanup.ATT_COLOR) + " " + ((CleanupRoom)room).name);
//				s.addObject(new CleanupBlock(id, bX, bY, shape, color));
//				i = i + 1;
//			}
//		}

        return s;
    }


    @Override
    public State generateState() {
//		return generateFourRooms();
        return generateCentralRoomWithClosets(1);
    }

    public State generateFourRooms() {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 4;
        int numDoors = 4;

        int y1 = 3;
        int y2 = 7;
        int y3 = 12;

        int x1 = 4;
        int x2 = 8;
        int x3 = 12;

        int ax = 7;
        int ay = 1;
        String agentDirection = Cleanup.ACTION_NORTH;

        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);

//		s.addObject(new CleanupBlock("block0", bx, by, bShape, bColor));

        s.addObject(new CleanupRoom("room0", x1, x2, 0, y2, Cleanup.COLORS_BLOCKS[rng.nextInt(Cleanup.COLORS_BLOCKS.length)], Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupRoom("room1", 0, x1, y1, y2, Cleanup.COLORS_BLOCKS[rng.nextInt(Cleanup.COLORS_BLOCKS.length)], Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupRoom("room2", 0, x3, y2, y3, Cleanup.COLORS_BLOCKS[rng.nextInt(Cleanup.COLORS_BLOCKS.length)], Cleanup.SHAPE_ROOM));
        s.addObject(new CleanupRoom("room3", x2, x3, 0, y2, Cleanup.COLORS_BLOCKS[rng.nextInt(Cleanup.COLORS_BLOCKS.length)], Cleanup.SHAPE_ROOM));

        s.addObject(new CleanupDoor("door0", x2, x2, 1, 1, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door1", x1, x1, 5, 5, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door2", 2, 2, y2, y2, Cleanup.LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door3", 10, 10, y2, y2, Cleanup.LOCKABLE_STATES[0]));


        int i = 0;
        while (i < numBlocks) {
            String id = "block" + i;
            CleanupRoom room = (CleanupRoom) s.objectsOfClass(Cleanup.CLASS_ROOM).get(rng.nextInt(numRooms));
            int rLeft = ((Integer) room.get(ATT_LEFT)) + 1;
            int rRight = ((Integer) room.get(ATT_RIGHT)) - 1;
            int rTop = ((Integer) room.get(ATT_TOP)) - 1;
            int rBottom = ((Integer) room.get(ATT_BOTTOM)) + 1;
            int bX = rng.nextInt(rRight - rLeft) + rLeft;
            int bY = rng.nextInt(rTop - rBottom) + rBottom;
            if (s.isOpen(bX, bY)) {
                String shape = Cleanup.SHAPES[rng.nextInt(Cleanup.SHAPES.length)];
                String color = Cleanup.COLORS_BLOCKS[rng.nextInt(Cleanup.COLORS_BLOCKS.length)];
                DPrint.cl(DEBUG_CODE, "block" + i + ": " + shape + " " + color + " (" + bX + ", " + bY + ") in the " + room.get(Cleanup.ATT_COLOR) + " room");
                s.addObject(new CleanupBlock(id, bX, bY, shape, color));
                i = i + 1;
            }
        }
//		Cleanup.setBlock(s, 0, 5, 4, "chair", "blue");
//		Cleanup.setBlock(s, 1, 6, 10, "basket", "red");
//		Cleanup.setBlock(s, 2, 2, 10, "bag", "magenta");

        return s;


    }

    public static boolean regionContainsPoint(ObjectInstance o, int x, int y, boolean countBoundary) {
        int top = (Integer) o.get(ATT_TOP);
        int left = (Integer) o.get(ATT_LEFT);
        int bottom = (Integer) o.get(ATT_BOTTOM);
        int right = (Integer) o.get(ATT_RIGHT);

        if (countBoundary) {
            if (y >= bottom && y <= top && x >= left && x <= right) {
                return true;
            }
        } else {
            if (y > bottom && y < top && x > left && x < right) {
                return true;
            }
        }

        return false;
    }

    public static CleanupGoalDescription[] getRandomGoalDescription(CleanupState s, int numGoals, PropositionalFunction pf) {
        return getRandomGoalDescription(s, numGoals, pf, RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX));
    }

    public static CleanupGoalDescription[] getRandomGoalDescription(CleanupState s, int numGoals, PropositionalFunction pf, Random rng) {
        CleanupGoalDescription[] goals = new CleanupGoalDescription[numGoals];
        if (pf.getName().equals(Cleanup.PF_BLOCK_IN_ROOM)) {
            List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
            List<ObjectInstance> rooms = s.objectsOfClass(Cleanup.CLASS_ROOM);
            List<Integer> blockIdxs = new ArrayList<Integer>();
            for (int i = 0; i < blocks.size(); i++) {
                blockIdxs.add(i);
            }
            for (int i = 0; i < numGoals; i++) {
                ObjectInstance block = blocks.get(blockIdxs.get(i));
                ObjectInstance room = rooms.get(rng.nextInt(rooms.size()));
                while (regionContainsPoint(room, (Integer) block.get(Cleanup.ATT_X), (Integer) block.get(Cleanup.ATT_Y), true)) {
                    // disallow the room the block is already in
                    room = rooms.get(rng.nextInt(rooms.size()));
                }
                goals[i] = new CleanupGoalDescription(new String[]{block.name(), room.name()}, pf);
                DPrint.cl(DEBUG_CODE, goals[i] + ": "
                        + block.get(Cleanup.ATT_COLOR) + " "
                        + block.get(Cleanup.ATT_SHAPE) + " to "
                        + room.get(Cleanup.ATT_COLOR) + " room");
            }
        } else if (pf.getName().equals(Cleanup.PF_AGENT_IN_DOOR)) {
            List<ObjectInstance> agents = s.objectsOfClass(Cleanup.CLASS_AGENT);
            List<ObjectInstance> doors = s.objectsOfClass(Cleanup.CLASS_DOOR);
            for (int i = 0; i < numGoals; i++) {
                ObjectInstance door = doors.get(rng.nextInt(doors.size()));
                ObjectInstance agent = agents.get(0);
                goals[i] = new CleanupGoalDescription(new String[]{agent.name(), door.name()}, pf);
                DPrint.cl(DEBUG_CODE, goals[i] + ": agent (x:"
                        + agent.get(Cleanup.ATT_X) + ", y:"
                        + agent.get(Cleanup.ATT_Y) + ") to door (x:"
                        + door.get(Cleanup.ATT_X) + ", y:"
                        + door.get(Cleanup.ATT_Y) + ")");
            }
        } else if (pf.getName().equals(Cleanup.PF_BLOCK_IN_DOOR)) {
            List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
            List<ObjectInstance> doors = s.objectsOfClass(Cleanup.CLASS_DOOR);
            for (int i = 0; i < numGoals; i++) {
                ObjectInstance door = doors.get(rng.nextInt(doors.size()));
                ObjectInstance block = blocks.get(0);
                goals[i] = new CleanupGoalDescription(new String[]{block.name(), door.name()}, pf);
                DPrint.cl(DEBUG_CODE, goals[i] + ": block (x:"
                        + block.get(Cleanup.ATT_X) + ", y:"
                        + block.get(Cleanup.ATT_Y) + ") to door (x:"
                        + door.get(Cleanup.ATT_X) + ", y:"
                        + door.get(Cleanup.ATT_Y) + ")");
            }
        } else {
            throw new RuntimeException("Randomization of goal not implemented for given propositional function.");
        }
        return goals;
    }


    public static CleanupGoalDescription[] getGoalDescriptionBlockToRoomSameColor(CleanupState s, int numGoals, PropositionalFunction pf) {
        CleanupGoalDescription[] goals = new CleanupGoalDescription[numGoals];
        List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
        List<ObjectInstance> rooms = s.objectsOfClass(Cleanup.CLASS_ROOM);
        List<Integer> blockIdxs = new ArrayList<Integer>();
        for (int i = 0; i < blocks.size(); i++) {
            blockIdxs.add(i);
        }

        // temp debug, reverse block order to get last block added to domain as the initial goal
        Collections.reverse(blockIdxs);

        for (int i = 0; i < numGoals; i++) {
            CleanupBlock block = (CleanupBlock) blocks.get(blockIdxs.get(i));
            String blockColor = (String) block.get(Cleanup.ATT_COLOR);
            for (int j = 0; j < rooms.size(); j++) {
                CleanupRoom room = (CleanupRoom) rooms.get(j);
                String roomColor = (String) room.get(Cleanup.ATT_COLOR);
                if (blockColor.equals(roomColor)) {
                    goals[i] = new CleanupGoalDescription(new String[]{block.name(), room.name()}, pf);
                    DPrint.cl(DEBUG_CODE, goals[i] + ": "
                            + block.get(Cleanup.ATT_COLOR) + " "
                            + block.get(Cleanup.ATT_SHAPE) + " to "
                            + room.get(Cleanup.ATT_COLOR) + " " + ((CleanupRoom) room).getName());
                    break;
                }
            }
            if (goals[i] == null) {
                throw new RuntimeException("Error: unable to find a room of block's color, " + blockColor);
            }
        }
        return goals;
    }

    public static CleanupGoalDescription[] getGoalDescriptionBlockToDoorSameColor(CleanupState s, int numGoals, PropositionalFunction pf) {
        CleanupGoalDescription[] goals = new CleanupGoalDescription[numGoals];
        List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
        List<ObjectInstance> doors = s.objectsOfClass(Cleanup.CLASS_DOOR);
        List<Integer> blockIdxs = new ArrayList<Integer>();
        for (int i = 0; i < blocks.size(); i++) {
            blockIdxs.add(i);
        }
        for (int i = 0; i < numGoals; i++) {
            CleanupBlock block = (CleanupBlock) blocks.get(blockIdxs.get(i));
            String blockColor = (String) block.get(Cleanup.ATT_COLOR);
            for (int j = 0; j < doors.size(); j++) {
                CleanupDoor door = (CleanupDoor) doors.get(j);
                String doorColor = (String) door.get(Cleanup.ATT_COLOR);
                if (blockColor.equals(doorColor)) {
                    goals[i] = new CleanupGoalDescription(new String[]{block.name(), door.name()}, pf);
                    DPrint.cl(DEBUG_CODE, goals[i] + ": "
                            + block.get(Cleanup.ATT_COLOR) + " "
                            + block.get(Cleanup.ATT_SHAPE) + " to "
                            + door.get(Cleanup.ATT_COLOR) + " " + ((CleanupDoor) door).getName());
                    break;
                }
            }
            if (goals[i] == null) {
                throw new RuntimeException("Error: unable to find a room of block's color, " + blockColor);
            }
        }
        return goals;
    }

    private State generateSlidingBlockPuzzle() {
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 1;

        List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));

        int bigRoomLeft = minX;
        int bigRoomRight = maxX-1;
        int bigRoomBottom = minY;
        int bigRoomTop = maxY-1;
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        String room2Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room0", bigRoomLeft, bigRoomRight/2, bigRoomBottom, bigRoomTop, room1Color, Cleanup.SHAPE_ROOM);
        CleanupRoom room2 = new CleanupRoom("room1", bigRoomRight/2, bigRoomRight, bigRoomBottom, bigRoomTop, room2Color, Cleanup.SHAPE_ROOM);
        int dx2 = bigRoomRight/2;
        int dy2 = bigRoomTop/2;
        CleanupDoor door2 = new CleanupDoor("door0", dx2, dx2, dy2, dy2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        // randomize agent's position
        int ax = minX;
        int ay = minY;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);
        s.addObject(room1);
        s.addObject(room2);
        s.addObject(door2);
        do {
            ax = minX + rng.nextInt(getWidth());
            ay = minY + rng.nextInt(getHeight());
        } while (!s.isOpen(ax, ay));
        s.getAgent().set(Cleanup.ATT_X, ax);
        s.getAgent().set(Cleanup.ATT_Y, ay);

        int index = 0;
        for (int i = (int) room1.get(ATT_LEFT); i < (int) room1.get(ATT_RIGHT); i++) {
            for (int j = (int) room1.get(ATT_BOTTOM); j < (int) room1.get(ATT_TOP); j++) {
                int bx = i;
                int by = j;
                if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                    String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
                    String color = blockColors.get(rng.nextInt(blockColors.size()));
                    s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                    index += 1;
                }
            }
        }

        return s;
    }

    public OOState generateTwoRoomsOneDoor() {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numBlocks = 0;
        int numRooms = 2;
        int numDoors = 1;

        int mx = (getWidth() / 2);
        int my = (getHeight() / 2);
        int maxRadiusWidth = mx - 2;
        int maxRadiusHeight = my - 2;
        int mainW = 1 + rng.nextInt(maxRadiusWidth);
        int mainH = 1 + rng.nextInt(maxRadiusHeight);
        int availableW = maxRadiusWidth - mainW;
        int availableH = maxRadiusHeight - mainH;

        int ax = mx + rng.nextInt(availableW + 1);
        int ay = my + rng.nextInt(availableH + 1);
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));


        int bigRoomRadiusWidth = mx;
        int bigRoomRadiusHeight = my;
        String bigRoomColor = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom bigRoom = new CleanupRoom("room1", mx - bigRoomRadiusWidth, mx + bigRoomRadiusWidth, my - bigRoomRadiusHeight, my + bigRoomRadiusHeight, bigRoomColor, Cleanup.SHAPE_ROOM);


        String roomColor = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room = new CleanupRoom("room0", ax - mainW, ax + mainW, ay - mainH, ay + mainH, roomColor, Cleanup.SHAPE_ROOM);
        int rx = ((Integer) room.get(ATT_LEFT));
        int ry = ((Integer) room.get(ATT_BOTTOM));
        int rWidth = ((Integer) room.get(ATT_RIGHT)) - ((Integer) room.get(ATT_LEFT));
        int rHeight = ((Integer) room.get(ATT_TOP)) - ((Integer) room.get(ATT_BOTTOM));
        boolean leftOrBottom = rng.nextBoolean();
        int dx = 0;
        int dy = 0;
        boolean onVerticalWall = rng.nextBoolean();
        if (onVerticalWall) {
            dx = leftOrBottom ? rx : rx + rWidth;
            dy = 1 + ry + rng.nextInt(rHeight - 1);
        } else {
            dx = 1 + rx + rng.nextInt(rWidth - 1);
            dy = leftOrBottom ? ry : ry + rHeight;
        }
        CleanupDoor door = new CleanupDoor("door0", dx, dx, dy, dy, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        // randomize agent's position
        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);
        s.addObject(bigRoom);
        s.addObject(room);
        s.addObject(door);
        do {
            ax = rng.nextInt(getWidth());
            ay = rng.nextInt(getHeight());
        } while (!s.isOpen(ax, ay));
        s.getAgent().set(Cleanup.ATT_X, ax);
        s.getAgent().set(Cleanup.ATT_Y, ay);

        return s;
    }

    public OOState generateThreeRooms(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 3;
        int numDoors = 3;

        List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));

        int bigRoomLeft = minX;
       //System.out.println("bigRoomsLeft is: "+ bigRoomLeft);
        int bigRoomRight = maxX-1;
        //System.out.println("bigRoomsLeft is: "+ bigRoomRight);
        int bigRoomBottom = maxY/2;
        //System.out.println("bigRoomsLeft is: "+ bigRoomBottom);
        int bigRoomTop = maxY-1;
        //System.out.println("bigRoomsLeft is: "+ bigRoomTop);
        String bigRoomColor = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom bigRoom = new CleanupRoom("room0", bigRoomLeft, bigRoomRight, bigRoomBottom, bigRoomTop, bigRoomColor, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        String room2Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", bigRoomLeft, bigRoomRight/2, minY, bigRoomBottom, room1Color, Cleanup.SHAPE_ROOM);
        CleanupRoom room2 = new CleanupRoom("room2", bigRoomRight/2, bigRoomRight, minY, bigRoomBottom, room2Color, Cleanup.SHAPE_ROOM);
        int dx0 = bigRoomRight/3;
        int dx1 = 2*bigRoomRight/3 + 1;
        int dx2 = bigRoomRight/2;
        int dy0 = bigRoomBottom;
        int dy1 = bigRoomBottom;
        int dy2 = bigRoomBottom/2;
        CleanupDoor door0 = new CleanupDoor("door0", dx0, dx0, dy0, dy0, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door1 = new CleanupDoor("door1", dx1, dx1, dy1, dy1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door2 = new CleanupDoor("door2", dx2, dx2, dy2, dy2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        // randomize agent's position
        int ax = minX;
        int ay = minY;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);
        s.addObject(bigRoom);
        s.addObject(room1);
        s.addObject(room2);
        s.addObject(door0);
        s.addObject(door1);
        s.addObject(door2);
        do {
            ax = minX + rng.nextInt(getWidth());
            ay = minY + rng.nextInt(getHeight());
        } while (!s.isOpen(ax, ay));
        s.getAgent().set(Cleanup.ATT_X, ax);
        s.getAgent().set(Cleanup.ATT_Y, ay);

        int index = 0;
        while (numBlocks > 0) {
            int bx = minY + rng.nextInt(getWidth());
            int by = minY + rng.nextInt(getHeight());
            if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
                String color = blockColors.get(rng.nextInt(blockColors.size()));
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                numBlocks -= 1;
                index += 1;
            }
        }

        return s;
    }

    public OOState generateTwoRooms(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 1;

        List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));

        int bigRoomLeft = minX;
        int bigRoomRight = maxX-1;
        int bigRoomBottom = minY;
        int bigRoomTop = maxY-1;
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        String room2Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room0", bigRoomLeft, bigRoomRight/2, bigRoomBottom, bigRoomTop, room1Color, Cleanup.SHAPE_ROOM);
        CleanupRoom room2 = new CleanupRoom("room1", bigRoomRight/2, bigRoomRight, bigRoomBottom, bigRoomTop, room2Color, Cleanup.SHAPE_ROOM);
        int dx2 = bigRoomRight/2;
        int dy2 = bigRoomTop/2;
        CleanupDoor door2 = new CleanupDoor("door0", dx2, dx2, dy2, dy2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        // randomize agent's position
        int ax = minX;
        int ay = minY;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);
        s.addObject(room1);
        s.addObject(room2);
        s.addObject(door2);
        do {
            ax = minX + rng.nextInt(getWidth());
            ay = minY + rng.nextInt(getHeight());
        } while (!s.isOpen(ax, ay));
        s.getAgent().set(Cleanup.ATT_X, ax);
        s.getAgent().set(Cleanup.ATT_Y, ay);

        int index = 0;
        while (numBlocks > 0) {
            int bx = minY + rng.nextInt(bigRoomRight/2);
            int by = minY + rng.nextInt(bigRoomTop);
            if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
                String color = blockColors.get(rng.nextInt(blockColors.size()));
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                numBlocks -= 1;
                index += 1;
            }
        }

        return s;
    }

    public OOState generateOneRoomOneDoor() {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numBlocks = 0;
        int numRooms = 1;
        int numDoors = 1;

        int maxRadiusWidth = 4;
        int maxRadiusHeight = 4;
        int mainW = 1 + rng.nextInt(maxRadiusWidth);
        int mainH = 1 + rng.nextInt(maxRadiusHeight);
        int availableW = maxRadiusWidth - mainW;
        int availableH = maxRadiusHeight - mainH;
        int mx = (getWidth() / 2) + rng.nextInt(availableW + 1);
        int my = (getHeight() / 2) + rng.nextInt(availableH + 1);

        int ax = mx;
        int ay = my;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));


        int index = 0;
        while (numBlocks > 0) {
            int bx = ax + (rng.nextBoolean() ? -1 : 1);
            int by = ay + (rng.nextBoolean() ? -1 : 1);
            if (!s.blockAt(bx, by)) {
                s.addObject(new CleanupBlock("block" + index, bx, by, "backpack", blockColors.get(rng.nextInt(blockColors.size()))));
                numBlocks -= 1;
                index += 1;
            }
        }


        String roomColor = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room = new CleanupRoom("room0", mx - mainW, mx + mainW, my - mainH, my + mainH, roomColor, Cleanup.SHAPE_ROOM);
        int rx = ((Integer) room.get(ATT_LEFT));
        int ry = ((Integer) room.get(ATT_BOTTOM));
        int rWidth = ((Integer) room.get(ATT_RIGHT)) - ((Integer) room.get(ATT_LEFT));
        int rHeight = ((Integer) room.get(ATT_TOP)) - ((Integer) room.get(ATT_BOTTOM));
        boolean leftOrBottom = rng.nextBoolean();
        int dx = 0;
        int dy = 0;
        boolean onVerticalWall = rng.nextBoolean();
        if (onVerticalWall) {
            dx = leftOrBottom ? rx : rx + rWidth;
            dy = 1 + ry + rng.nextInt(rHeight - 1);
        } else {
            dx = 1 + rx + rng.nextInt(rWidth - 1);
            dy = leftOrBottom ? ry : ry + rHeight;
        }
        CleanupDoor door = new CleanupDoor("door0", dx, dx, dy, dy, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        s.addObject(room);
        s.addObject(door);

        return s;
    }

    public OOState generateNoRoomsOneDoor(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 0;
        int numDoors = 1;

        int ax = minX + rng.nextInt(getWidth());
        int ay = minY + rng.nextInt(getHeight());
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));

        int dx = minX + rng.nextInt(getWidth());
        int dy = minY + rng.nextInt(getHeight());
        CleanupDoor door = new CleanupDoor("door0", dx, dx, dy, dy, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        // randomize agent's position
        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);
        s.addObject(door);
        int index = 0;
        while (numBlocks > 0) {
            int bx = minX + rng.nextInt(getWidth());
            int by = minY + rng.nextInt(getHeight());
            if (!s.blockAt(bx, by) && !s.agentAt(bx, by)) {
                String color = blockColors.get(rng.nextInt(blockColors.size()));
                String shape = Cleanup.SHAPES[rng.nextInt(Cleanup.SHAPES.length)];
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                numBlocks -= 1;
                index += 1;
            }
        }
        do {
            ax = rng.nextInt(getWidth());
            ay = rng.nextInt(getHeight());
        } while (!s.isOpen(ax, ay));
        s.getAgent().set(Cleanup.ATT_X, ax);
        s.getAgent().set(Cleanup.ATT_Y, ay);

        return s;
    }

    public OOState generateDonutCheckersRooms(int numBlocks){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 9;
        int numDoors = 12;

        List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        int[] rb = new int[]{0,2,4};
        int i=0;
        int  rl=0;
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", rl, rl+2, rb[i], rb[i]+2, room0Color, Cleanup.SHAPE_ROOM);
        i++;
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", rl, rl+2, rb[i], rb[i]+2, room1Color, Cleanup.SHAPE_ROOM);
        i++;
        String room2Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room2 = new CleanupRoom("room2", rl, rl+2, rb[i], rb[i]+2, room2Color, Cleanup.SHAPE_ROOM);
        rl+=2;
        i=0;

        String room3Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room3 = new CleanupRoom("room3", rl, rl+2, rb[i], rb[i]+2, room3Color, Cleanup.SHAPE_ROOM);
        i++;
        String room4Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room4 = new CleanupRoom("room4", rl, rl+2, rb[i], rb[i]+2, room4Color, Cleanup.SHAPE_ROOM);
        i++;
        String room5Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room5 = new CleanupRoom("room5", rl, rl+2, rb[i], rb[i]+2, room5Color, Cleanup.SHAPE_ROOM);
        rl+=2;
        i=0;

        String room6Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room6 = new CleanupRoom("room6", rl, rl+2, rb[i], rb[i]+2, room6Color, Cleanup.SHAPE_ROOM);
        i++;
        String room7Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room7 = new CleanupRoom("room7", rl, rl+2, rb[i], rb[i]+2, room7Color, Cleanup.SHAPE_ROOM);
        i++;
        String room8Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room8 = new CleanupRoom("room8", rl, rl+2, rb[i], rb[i]+2, room8Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        int[] db = new int[]{2,4,1,3,5};
        i=0;
        int dl=1;
        CleanupDoor door0= new CleanupDoor("door0", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;
        CleanupDoor door1= new CleanupDoor("door1", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;

        dl++;
        CleanupDoor door2= new CleanupDoor("door2", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;
        CleanupDoor door3= new CleanupDoor("door3", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;
        CleanupDoor door4= new CleanupDoor("door4", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i=0;

        dl++;
        CleanupDoor door5= new CleanupDoor("door5", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;
        CleanupDoor door6= new CleanupDoor("door6", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;

        dl++;
        CleanupDoor door7= new CleanupDoor("door7", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;
        CleanupDoor door8= new CleanupDoor("door8", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;
        CleanupDoor door9= new CleanupDoor("door9", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i=0;

        dl++;
        CleanupDoor door10= new CleanupDoor("door10", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        i++;
        CleanupDoor door11= new CleanupDoor("door11", dl, dl, db[i], db[i], Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);



        //initialize block
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 3, 1, shape, color);

        //initialize agent
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 1, 1, ACTION_SOUTH, numBlocks, numRooms, numDoors);

        s.addObject(room0);
        s.addObject(room1);
        s.addObject(room2);
        s.addObject(room3);
        s.addObject(room4);
        s.addObject(room5);
        s.addObject(room6);
        s.addObject(room7);
        s.addObject(room8);

        s.addObject(door0);
        s.addObject(door1);
        s.addObject(door2);
        s.addObject(door3);
        s.addObject(door4);
        s.addObject(door5);
        s.addObject(door6);
        s.addObject(door7);
        s.addObject(door8);
        s.addObject(door9);
        s.addObject(door10);
        s.addObject(door11);

        s.addObject(block0);

        s.getAgent().set(Cleanup.ATT_X, 1);
        s.getAgent().set(Cleanup.ATT_Y, 1);

//        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
//        String color = blockColors.get(rng.nextInt(blockColors.size()));
//        s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));

        return s;
    }

    public OOState generateDonutRooms(){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 14;
        int numDoors = 4;

        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 1, 1, agentDirection, 1, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", 0, 2, 0, 2, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", 0, 2, 2, 4, room1Color, Cleanup.SHAPE_ROOM);
        String room2Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room2 = new CleanupRoom("room2", 2, 4, 0, 2, room2Color, Cleanup.SHAPE_ROOM);
        String room3Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room3 = new CleanupRoom("room3", 2, 4, 2, 4, room3Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", 1, 1, 2, 2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door1 = new CleanupDoor("door1", 2, 2, 1, 1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door2 = new CleanupDoor("door2", 2, 2, 3, 3, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door3 = new CleanupDoor("door3", 3, 3, 2, 2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        //initialize block
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 3, 1, shape, color);

        s.addObject(room0);
        s.addObject(room1);
        s.addObject(room2);
        s.addObject(room3);
        s.addObject(door0);
        s.addObject(door1);
        s.addObject(door2);
        s.addObject(door3);
        s.addObject(block0);

        return s;
    }

    public OOState generateClassic(){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 4;
        int numDoors = 4;

        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 1, 1, agentDirection, 1, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", 0, 4, 6, 9, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", 0, 4, 0, 6, room1Color, Cleanup.SHAPE_ROOM);
        String room2Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room2 = new CleanupRoom("room2", 4, 8, 0, 4, room2Color, Cleanup.SHAPE_ROOM);
        String room3Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room3 = new CleanupRoom("room3", 4, 8, 4, 9, room3Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", 3, 3, 6, 6, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door1 = new CleanupDoor("door1", 4, 4, 5, 5, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door2 = new CleanupDoor("door2", 4, 4, 2, 2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door3 = new CleanupDoor("door3", 6, 6, 4, 4, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        //initialize block
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 3, 1, shape, color);

        s.addObject(room0);
        s.addObject(room1);
        s.addObject(room2);
        s.addObject(room3);
        s.addObject(door0);
        s.addObject(door1);
        s.addObject(door2);
        s.addObject(door3);
        s.addObject(block0);

        return s;
    }

    public OOState generate1blockDebris(){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 1;

        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 1, 1, agentDirection, 1, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", 0, 3, 0, 3, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", 3, 6, 0, 3, room1Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", 3, 3, 1, 1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);


        //initialize blocks
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 1, 2, shape, color);
        CleanupBlock block1 = new CleanupBlock("block1", 4, 1, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(rng.nextInt(blockColors.size())));

        s.addObject(room0);
        s.addObject(room1);

        s.addObject(door0);

        s.addObject(block0);
        s.addObject(block1);

        return s;
    }

    public OOState generate2blockDebris(){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 1;

        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 1, 1, agentDirection, 1, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", 0, 3, 0, 3, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", 3, 6, 0, 3, room1Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", 3, 3, 1, 1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);


        //initialize blocks
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 1, 2, shape, color);
        CleanupBlock block1 = new CleanupBlock("block1", 4, 1, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(rng.nextInt(blockColors.size())));
        CleanupBlock block2 = new CleanupBlock("block2", 3, 1, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(rng.nextInt(blockColors.size())));

        s.addObject(room0);
        s.addObject(room1);

        s.addObject(door0);

        s.addObject(block0);
        s.addObject(block1);
        s.addObject(block2);

        return s;
    }

    public OOState generate2blockSolve(){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 1;

        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 1, 1, agentDirection, 1, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", 0, 4, 0, 4, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", 4, 8, 0, 4, room1Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", 4, 4, 2, 2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);


        //initialize blocks
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 1, 2, shape, color);
        CleanupBlock block1 = new CleanupBlock("block1", 1, 3, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(rng.nextInt(blockColors.size())));
        CleanupBlock block2 = new CleanupBlock("block2", 4, 2, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(rng.nextInt(blockColors.size())));

        s.addObject(room0);
        s.addObject(room1);

        s.addObject(door0);

        s.addObject(block0);
        s.addObject(block1);
        s.addObject(block2);

        return s;
    }

    public OOState generate1blockCorner(){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 1;

        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 2, 2, agentDirection, 1, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", 0, 4, 0, 4, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", 4, 8, 0, 4, room1Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", 4, 4, 2, 2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);


        //initialize blocks
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 1, 1, shape, color);
        CleanupBlock block1 = new CleanupBlock("block1", 1, 2, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(rng.nextInt(blockColors.size())));
        CleanupBlock block2 = new CleanupBlock("block2", 2, 1, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(rng.nextInt(blockColors.size())));

        s.addObject(room0);
        s.addObject(room1);

        s.addObject(door0);

        s.addObject(block0);
        s.addObject(block1);
        s.addObject(block2);

        return s;
    }

    public OOState generateSpiral(){
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 5;
        int numDoors = 4;

        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), 1, 7, agentDirection, 1, numRooms, numDoors);

        List<String> blockColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<String>(Arrays.asList(Cleanup.COLORS_ROOMS));

        //initializes rooms
        String room0Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room0 = new CleanupRoom("room0", 0, 6, 6, 9, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room1 = new CleanupRoom("room1", 6, 9, 0, 9, room1Color, Cleanup.SHAPE_ROOM);
        String room2Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room2 = new CleanupRoom("room2", 0, 6, 0, 3, room2Color, Cleanup.SHAPE_ROOM);
        String room3Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room3 = new CleanupRoom("room3", 0, 3, 3, 6, room3Color, Cleanup.SHAPE_ROOM);
        String room4Color = roomColors.get(rng.nextInt(roomColors.size()));
        CleanupRoom room4 = new CleanupRoom("room4", 3, 6, 3, 6, room4Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", 6, 6, 7, 8, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door1 = new CleanupDoor("door1", 6, 6, 1, 2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door2 = new CleanupDoor("door2", 1, 2, 3, 3, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door3 = new CleanupDoor("door3", 3, 3, 4, 5, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);


        //initialize blocks
        String shape = blockShapes.get(rng.nextInt(blockShapes.size()));
        String color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupBlock block0 = new CleanupBlock("block0", 1, 8, shape, color);

        s.addObject(room0);
        s.addObject(room1);
        s.addObject(room2);
        s.addObject(room3);
        s.addObject(room4);

        s.addObject(door0);
        s.addObject(door1);
        s.addObject(door2);
        s.addObject(door3);

        s.addObject(block0);

        return s;
    }

    public OOState generateCigar(int numBlocks){

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        int numDoors = 1;
        //one room is filled with blocks, the other room has space for all blocks, and there is 1 door and 2 walls
        int width = numBlocks*2+3;
        //2 walls and 1 corridor
        int height = 3;
        String agentDirection = Cleanup.directions[3];
        CleanupState s = new CleanupState(width, height, numBlocks+1, 1, agentDirection, numBlocks, numRooms, numDoors);

        List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        List<String> blockShapes = new ArrayList<>(Arrays.asList(Cleanup.SHAPES_BLOCKS));
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));
        CleanupRoom[] rooms = new CleanupRoom[2];
        //initializes rooms
        //white
        String room0Color = roomColors.get(7);
        rooms[0] = new CleanupRoom("room0", 0, numBlocks+1, 0, height-1, room0Color, Cleanup.SHAPE_ROOM);
        //orange
        String room1Color = roomColors.get(5);
        //the right bound of room1 is
        rooms[1] = new CleanupRoom("room1", numBlocks+1, numBlocks*2+2, 0, height-1, room1Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", numBlocks+1, numBlocks+1, 1, 1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        //initialize blocks
        CleanupBlock[] blocks = new CleanupBlock[numBlocks];
        for(int i = 0; i < numBlocks; i++){
            blocks[i] = new CleanupBlock("block"+i,i+1,1, blockShapes.get(rng.nextInt(blockShapes.size())), blockColors.get(0));
        }

        for(CleanupRoom cr : rooms) { s.addObject(cr); }
        s.addObject(door0);
        for(CleanupBlock cb : blocks) { s.addObject(cb); }

        return s;
    }


    public State getStateFor(String stateType) {
        String blockNumberRegex = "\\-(\\d+)blocks";
        Pattern r = Pattern.compile(blockNumberRegex);
        Matcher m = r.matcher(stateType);
        String numString = "";
        if (m.find()) {
            numString = m.group(1);
        }
        if (numString.equals("")) {
            //if block number is not specified, default 1 block
            numBlocks = 1;
            stateType+="-1blocks";
        }
        State state;
        if (stateType.matches("oneRoomOneDoor" + blockNumberRegex)) {
            state = generateOneRoomOneDoor();
        } else if (stateType.matches("noRoomsOneDoor" + blockNumberRegex)) {
            state = generateNoRoomsOneDoor(numBlocks);
        } else if (stateType.matches("slidingBlock" + blockNumberRegex)) {
            state = generateSlidingBlockPuzzle();
        } else if (stateType.matches("twoRoomsOneDoor" + blockNumberRegex)) {
            state = generateTwoRoomsOneDoor();
        } else if (stateType.matches("oneRoomFourDoors" + blockNumberRegex)) {
            state = generateCentralRoomWithFourDoors(numBlocks);
        } else if (stateType.matches("twoRoomsFourDoors" + blockNumberRegex)) {
            state = generateTwoRoomsWithFourDoors(numBlocks);
        } else if (stateType.matches("threeRooms" + blockNumberRegex)){
            state = generateThreeRooms(numBlocks);
        } else if (stateType.matches("twoRooms" + blockNumberRegex)){
            state = generateTwoRooms(numBlocks);
        } else if (stateType.matches("donutCheckers" + blockNumberRegex)){
            state = generateDonutCheckersRooms(numBlocks);
        } else if (stateType.matches("donutRooms" + blockNumberRegex)){
            state = generateDonutRooms();
        } else if (stateType.matches("classic" + blockNumberRegex)){
            state = generateClassic();
        } else if (stateType.matches("1blockDebris" + blockNumberRegex)){
            state = generate1blockDebris();
        } else if (stateType.matches("2blockDebris" + blockNumberRegex)){
            state = generate2blockDebris();
        } else if (stateType.matches("2blockSolve" + blockNumberRegex)){
            state = generate2blockSolve();
        } else if (stateType.matches("1blockCorner" + blockNumberRegex)){
            state = generate1blockCorner();
        } else if (stateType.matches("spiral" + blockNumberRegex)){
            state = generateSpiral();
        } else if (stateType.matches("cigar" + blockNumberRegex)){
            state = generateCigar(numBlocks);
        } else {
            throw new RuntimeException("Error: unknown name for generating a random Cleanup state: " + stateType);
        }
        return state;
    }

}
