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
    private static final ArrayList targetBlockShapes = new ArrayList(2);
    private static final ArrayList blockShapes = new ArrayList(2);
    private static final List<String> blockColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_BLOCKS));
    private ArrayList<String> currentRoomColors = new ArrayList();
    private CleanupRoom[] rooms;
    private CleanupDoor[] doors;
    private CleanupBlock[] blocks;
    private int numBlocks = 2;
    private int numTargets = 1;
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

    void initShapeLists(){
        targetBlockShapes.add(Cleanup.SHAPES_BLOCKS[1]);
        targetBlockShapes.add(Cleanup.SHAPES_BLOCKS[2]);
        blockShapes.add(Cleanup.SHAPES_BLOCKS[0]);
        blockShapes.add(Cleanup.SHAPES_BLOCKS[3]);
    }

    public State getStateFor(String stateType) {
        String blockNumberRegex = "-(\\d+)blocks";
        String targetNumberRegex = "(\\d+)targets";
        Pattern r = Pattern.compile(blockNumberRegex), r2 = Pattern.compile(targetNumberRegex);
        Matcher m = r.matcher(stateType), m2 = r2.matcher(stateType);
        String numBlockString = m.find() ? m.group(1) : "";
        String numTargetString = m2.find() ? m2.group(1) : "";

        //if block number is not specified, default 1 block
        numBlocks = numBlockString.equals("") ? 1 : Integer.parseInt(numBlockString);
        //if target number is not specified, default 1 target
        numTargets = numTargetString.equals("") ? 1 : Integer.parseInt(numTargetString);
        if(numTargets>numBlocks) throw new RuntimeException("Error: cannot have more target blocks than blocks!");
        System.out.println(numBlocks+" blocks with "+numTargets+" targets");
        State state;
        if (stateType.startsWith("cigar")) {
            //for cigar, the number of targets is always the number of blocks
            //as all blocks must move to the other room
            state = generateCigar(numBlocks);
        }else if(stateType.startsWith("threeRooms")){
            state = generateThreeRooms(numBlocks, numTargets);
        }else if(stateType.startsWith("twoRooms")){
            state = generateTwoRooms(numBlocks, numTargets);
        }else if(stateType.startsWith("cross")){
            //for cross, the number of targets is always 1 (as one of the rooms is not reachable)
            state = generateCross(numBlocks);
        }else if(stateType.startsWith("donut")){
            state = generateDonut(numBlocks, numTargets);
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

        initShapeLists();
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));
        rooms = new CleanupRoom[2];
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
        blocks = new CleanupBlock[numBlocks];
        for(int i = 0; i < numBlocks; i++) {
            blocks[i] = new CleanupBlock("block" + i, i + 1, 1, (String) targetBlockShapes.get(rng.nextInt(2)), room1Color);
        }


        for(CleanupRoom cr : rooms) { s.addObject(cr); }
        s.addObject(door0);
        for(CleanupBlock cb : blocks) { s.addObject(cb); }

        return s;
    }

    public OOState generateThreeRooms(int numBlocks, int numTargets) {

        if(numTargets>3) throw new RuntimeException("Error: cannot have more blocks than rooms! Rooms: 3, Blocks: "+numBlocks);
        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 3;
        int numDoors = 3;

        initShapeLists();

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

        currentRoomColors.add((String) bigRoom.get(Cleanup.ATT_COLOR));
        currentRoomColors.add((String) room1.get(Cleanup.ATT_COLOR));
        currentRoomColors.add((String) room2.get(Cleanup.ATT_COLOR));
        int index = 0;
        int targetIndex = numTargets;
        while (targetIndex > 0) {
            int bx = minX + rng.nextInt(getWidth());
            int by = minY + rng.nextInt(getHeight());
            if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                String shape = (String) targetBlockShapes.get(rng.nextInt(2));
                String color = currentRoomColors.get(rng.nextInt(currentRoomColors.size()));
                String currentRoomColor = (String) s.regionContainingPoint(bx, by).get(Cleanup.ATT_COLOR);
                if(color.equals(currentRoomColor)){
                    continue;
                }
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                currentRoomColors.remove(color);
                targetIndex -= 1;
                index += 1;
            }
        }
        for(int i = numBlocks - numTargets; i > 0; i--){
            int bx;
            int by;
            do{
                bx = minX + rng.nextInt(getWidth());
                by = minY + rng.nextInt(getHeight());
            }while(!(s.isOpen(bx, by) && !s.agentAt(bx, by)));
            String shape = (String) blockShapes.get(rng.nextInt(2));
            String color = blockColors.get(rng.nextInt(blockColors.size()));
            s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
            index++;
        }

        return s;
    }

    public OOState generateTwoRooms(int numBlocks, int numTargets) {

        if(numTargets>2) throw new RuntimeException("Error: cannot have more blocks than rooms! Rooms: 2, Blocks: "+numBlocks);

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 2;
        //configurable
        int numDoors = 2;

        initShapeLists();

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


        currentRoomColors.add(room1Color);
        currentRoomColors.add(room2Color);
        int index = 0;
        int targetIndex = numTargets;
        while (targetIndex > 0) {
            int bx = minX + rng.nextInt(getWidth());
            int by = minY + rng.nextInt(getHeight());
            if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                String shape = (String) targetBlockShapes.get(rng.nextInt(2));
                String color = currentRoomColors.get(rng.nextInt(currentRoomColors.size()));
                String currentRoomColor = (String) s.regionContainingPoint(bx, by).get(Cleanup.ATT_COLOR);
                if(color.equals(currentRoomColor)){
                    continue;
                }
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                currentRoomColors.remove(color);
                targetIndex -= 1;
                index += 1;
            }
        }
        for(int i = numBlocks - numTargets; i > 0; i--){
            int bx;
            int by;
            do{
                bx = minX + rng.nextInt(getWidth());
                by = minY + rng.nextInt(getHeight());
            }while(!(s.isOpen(bx, by) && !s.agentAt(bx, by)));
            String shape = (String) blockShapes.get(rng.nextInt(2));
            String color = blockColors.get(rng.nextInt(blockColors.size()));
            s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
            index++;
        }

        return s;
    }

    public OOState generateCross(int numBlocks){

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 4;
        int numDoors = 3;
        //one room is filled with blocks, the other room has space for all blocks, and there is 1 door and 2 walls
        int width = 9;
        //2 walls and 1 corridor
        int height = 6;
        String agentDirection = Cleanup.directions[2];
        CleanupState s = new CleanupState(width, height, 1, 4, agentDirection, numBlocks, numRooms, numDoors);

        initShapeLists();

        rooms = new CleanupRoom[4];

        //initializes rooms
        String room0Color = "blue";
        rooms[0] = new CleanupRoom("room0", 0, 3, 3, 5, room0Color, Cleanup.SHAPE_ROOM);
        String room1Color = "magenta";
        rooms[1] = new CleanupRoom("room1", 3, 5, 3, 5, room1Color, Cleanup.SHAPE_ROOM);
        String room2Color = "green";
        rooms[2] = new CleanupRoom("room2", 5, 8, 3, 5, room2Color, Cleanup.SHAPE_ROOM);
        String room3Color = blockColors.get(rng.nextInt(blockColors.size()));
        rooms[3] = new CleanupRoom("room3", 3, 5, 0, 3, room3Color, Cleanup.SHAPE_ROOM);

        //initializes doors
        doors = new CleanupDoor[3];
        doors[0] = new CleanupDoor("door0", 3, 3, 4, 4, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        doors[1] = new CleanupDoor("door1", 5, 5, 4, 4, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
        doors[2] = new CleanupDoor("door2", 4, 4, 3, 3, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);

        //initialize blocks
        blocks = new CleanupBlock[1];
        blocks[0] = new CleanupBlock("block0", 2, 4, (String) blockShapes.get(rng.nextInt(blockShapes.size())), room2Color);


        for(CleanupRoom cr : rooms) { s.addObject(cr); }
        for(CleanupDoor dr : doors) { s.addObject(dr); }
        for(CleanupBlock cb : blocks) { s.addObject(cb); }

        return s;
    }

    public OOState generateDonut(int numBlocks, int numTargets){

        if(numTargets>3) throw new RuntimeException("Error: cannot have more blocks than rooms! Rooms: 3, Blocks: "+numBlocks);

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);

        int numRooms = 3;
        int numDoors = 3;

        initShapeLists();
        List<String> roomColors = new ArrayList<>(Arrays.asList(Cleanup.COLORS_ROOMS));

        maxX = 5;
        maxY = 5;
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

        currentRoomColors.add((String) bigRoom.get(Cleanup.ATT_COLOR));
        currentRoomColors.add((String) room1.get(Cleanup.ATT_COLOR));
        currentRoomColors.add((String) room2.get(Cleanup.ATT_COLOR));
        int index = 0;
        int targetIndex = numTargets;
        while (targetIndex > 0) {
            int bx = minX + rng.nextInt(getWidth());
            int by = minY + rng.nextInt(getHeight());
            if (s.isOpen(bx, by) && !s.agentAt(bx, by)) {
                String shape = (String) targetBlockShapes.get(rng.nextInt(2));
                String color = currentRoomColors.get(rng.nextInt(currentRoomColors.size()));
                String currentRoomColor = (String) s.regionContainingPoint(bx, by).get(Cleanup.ATT_COLOR);
                if(color.equals(currentRoomColor)){
                    continue;
                }
                s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
                currentRoomColors.remove(color);
                targetIndex -= 1;
                index += 1;
            }
        }
        for(int i = numBlocks - numTargets; i > 0; i--){
            int bx;
            int by;
            do{
                bx = minX + rng.nextInt(getWidth());
                by = minY + rng.nextInt(getHeight());
            }while(!(s.isOpen(bx, by) && !s.agentAt(bx, by)));
            String shape = (String) blockShapes.get(rng.nextInt(2));
            String color = blockColors.get(rng.nextInt(blockColors.size()));
            s.addObject(new CleanupBlock("block" + index, bx, by, shape, color));
            index++;
        }

        return s;
    }

}
