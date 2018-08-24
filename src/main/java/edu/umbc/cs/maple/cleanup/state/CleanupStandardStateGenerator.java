package edu.umbc.cs.maple.cleanup.state;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.utilities.BurlapConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanupStandardStateGenerator implements StateGenerator {

    private static final int DEBUG_CODE = 932891293;
    private int numBlocks = 2;
    private int minX = 0;
    private int minY = 0;
    private int maxX = 0;
    private int maxY = 0;

    public CleanupStandardStateGenerator(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public CleanupStandardStateGenerator(Cleanup cleanup) {
        this.minX = cleanup.getMinX();
        this.minY = cleanup.getMinY();
        this.maxX = cleanup.getMaxX();
        this.maxY = cleanup.getMaxY();
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

    @Override
    public State generateState() {
        CleanupRandomStateGenerator gen = new CleanupRandomStateGenerator(minX, minY, maxX, maxY);
        return gen.generateState();
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
            stateType += "-1blocks";
        } else {
            numBlocks = Integer.parseInt(numString);
        }
        State state;
        if (stateType.matches("cigar" + blockNumberRegex)) {
            state = generateCigar(numBlocks);
        }else if(stateType.matches("threeRooms" + blockNumberRegex)){
            state = generateThreeRooms(numBlocks);
        }else if(stateType.matches("twoRooms" + blockNumberRegex)){
            state = generateTwoRooms(numBlocks);
        } else {
            throw new RuntimeException("Error: unknown name for generating a random Cleanup state: " + stateType);
        }
        return state;
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
        ArrayList blockShapes = new ArrayList();
        blockShapes.add(Cleanup.SHAPES_BLOCKS[1]);
        blockShapes.add(Cleanup.SHAPES_BLOCKS[2]);
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));
        CleanupRoom[] rooms = new CleanupRoom[2];
        //initializes rooms
        //white
        String room0Color = roomColors.get(7);
        rooms[0] = new CleanupRoom("room0", 0, numBlocks+1, 0, height-1, room0Color, Cleanup.SHAPE_ROOM);
        //orange
        String room1Color = roomColors.get(4);
        //the right bound of room1 is
        rooms[1] = new CleanupRoom("room1", numBlocks+1, numBlocks*2+2, 0, height-1, room1Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        CleanupDoor door0 = new CleanupDoor("door0", numBlocks+1, numBlocks+1, 1, 1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        //initialize blocks
        CleanupBlock[] blocks = new CleanupBlock[numBlocks];
        for(int i = 0; i < numBlocks; i++) {
            blocks[i] = new CleanupBlock("block" + i, i + 1, 1, (String) blockShapes.get(rng.nextInt(2)), room1Color);
        }


        for(CleanupRoom cr : rooms) { s.addObject(cr); }
        s.addObject(door0);
        for(CleanupBlock cb : blocks) { s.addObject(cb); }

        return s;
    }

    public OOState generateThreeRooms(int numBlocks) {

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 3;
        int numDoors = 3;

        List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        ArrayList blockShapes = new ArrayList();
        blockShapes.add(Cleanup.SHAPES_BLOCKS[1]);
        blockShapes.add(Cleanup.SHAPES_BLOCKS[2]);
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));

        int bigRoomLeft = minX;
        //System.out.println("bigRoomsLeft is: "+ bigRoomLeft);
        int bigRoomRight = maxX-1;
        //System.out.println("bigRoomsLeft is: "+ bigRoomRight);
        int bigRoomBottom = maxY/2;
        //System.out.println("bigRoomsLeft is: "+ bigRoomBottom);
        int bigRoomTop = maxY-1;
        //System.out.println("bigRoomsLeft is: "+ bigRoomTop);
        String bigRoomColor = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupRoom bigRoom = new CleanupRoom("room0", bigRoomLeft, bigRoomRight, bigRoomBottom, bigRoomTop, bigRoomColor, Cleanup.SHAPE_ROOM);
        String room1Color = blockColors.get(rng.nextInt(blockColors.size()));
        String room2Color = blockColors.get(rng.nextInt(blockColors.size()));
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

        ArrayList<String> currentRoomColors = new ArrayList();
        currentRoomColors.add((String) bigRoom.get(Cleanup.ATT_COLOR));
        currentRoomColors.add((String) room1.get(Cleanup.ATT_COLOR));
        currentRoomColors.add((String) room2.get(Cleanup.ATT_COLOR));
        int index = 0;
        while (numBlocks > 0) {
            int bx = minX + rng.nextInt(getWidth());
            int by = minY + rng.nextInt(getHeight());
            if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                String shape = (String) blockShapes.get(rng.nextInt(2));
                String color = currentRoomColors.get(rng.nextInt(currentRoomColors.size()));
                String currentRoomColor = (String) s.regionContainingPoint(bx, by).get(Cleanup.ATT_COLOR);
                if(color.equals(currentRoomColor)){
                    continue;
                }
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
        //configurable
        int numDoors = 2;

        List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
        ArrayList blockShapes = new ArrayList();
        blockShapes.add(Cleanup.SHAPES_BLOCKS[1]);
        blockShapes.add(Cleanup.SHAPES_BLOCKS[2]);
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));

        int bigRoomLeft = minX;
        int bigRoomRight = maxX-1;
        int bigRoomBottom = minY;
        int bigRoomTop = maxY-1;
        String room1Color = blockColors.get(rng.nextInt(blockColors.size()));
        String room2Color = blockColors.get(rng.nextInt(blockColors.size()));
        CleanupRoom room1 = new CleanupRoom("room0", bigRoomLeft, bigRoomRight/2, bigRoomBottom, bigRoomTop, room1Color, Cleanup.SHAPE_ROOM);
        CleanupRoom room2 = new CleanupRoom("room1", bigRoomRight/2, bigRoomRight, bigRoomBottom, bigRoomTop, room2Color, Cleanup.SHAPE_ROOM);
        int dx1, dx2, dx3;
        dx1 = dx2 = dx3 = bigRoomRight/2;
        int dy2 = bigRoomTop/2;
        int dy1 = bigRoomTop-1;
        int dy3 = bigRoomBottom+1;
        CleanupDoor door1 = new CleanupDoor("door1", dx1, dx1, dy1, dy1,  Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
//        CleanupDoor door2 = new CleanupDoor("door0", dx2, dx2, dy2, dy2, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        CleanupDoor door3 = new CleanupDoor("door2", dx3, dx3, dy3, dy3, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        // randomize agent's position
        int ax = minX;
        int ay = minY;
        String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
        CleanupState s = new CleanupState(getWidth(), getHeight(), ax, ay, agentDirection, numBlocks, numRooms, numDoors);
        s.addObject(room1);
        s.addObject(room2);
//        s.addObject(door2);
        s.addObject(door1);
        s.addObject(door3);
        do {
            ax = minX + rng.nextInt(getWidth());
            ay = minY + rng.nextInt(getHeight());
        } while (!s.isOpen(ax, ay));
        s.getAgent().set(Cleanup.ATT_X, ax);
        s.getAgent().set(Cleanup.ATT_Y, ay);


        ArrayList<String> currentRoomColors = new ArrayList<>();
        currentRoomColors.add(room1Color);
        currentRoomColors.add(room2Color);
        int index = 0;
        while (numBlocks > 0) {
            int bx = minY + rng.nextInt(bigRoomRight/2);
            int by = minY + rng.nextInt(bigRoomTop);
            if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                String shape = (String) blockShapes.get(rng.nextInt(blockShapes.size()));
                String color = currentRoomColors.get(rng.nextInt(currentRoomColors.size()));
                String currentRoomColor = (String) s.regionContainingPoint(bx, by).get(Cleanup.ATT_COLOR);
                if(color.equals(currentRoomColor)){
                    continue;
                }
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                numBlocks -= 1;
                index += 1;
            }
        }

        return s;
    }

}
