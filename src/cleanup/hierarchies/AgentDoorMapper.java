package cleanup.hierarchies;

import amdp.cleanup.CleanupDomain;
import amdp.cleanup.state.*;
import amdp.cleanupamdpdomains.cleanuplevel1.state.*;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import cleanup.Cleanup;
import cleanup.state.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static cleanup.Cleanup.*;

public class AgentDoorMapper implements StateMapping{

    @Override
    public State mapState(State sIn) {

        CleanupState s = (CleanupState)sIn;

        //set agent position
        //first try room
        CleanupAgent agent = s.getAgent();
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        CleanupRoom inRoom = s.roomContainingPoint(ax, ay);

        String inRegion;
        if(inRoom != null){
            inRegion = inRoom.name();
        }
        else{
            CleanupDoor inDoor = s.doorContainingPoint(ax, ay);
            inRegion = inDoor.name();
        }


        MoveAgent abstractAgent = new MoveAgent(agent.name(), inRegion);

        List<CleanupRoom> rooms = (List<CleanupRoom>) s.getRooms().values();
        List<MoveRoom> abstractRooms = new ArrayList<MoveRoom>();
        for(CleanupRoom r : rooms){
            MoveRoom rL1 = new MoveRoom(r.name(), r.get(ATT_COLOR), new HashSet<String>());
            abstractRooms.add(rL1);
        }

        List<CleanupDoor> doors = (List<CleanupDoor>) s.getDoors();
        List<MoveDoor> abstractDoors = new ArrayList<MoveDoor>();
        for(CleanupDoor d : doors){
            MoveDoor ad = new MoveDoor(d.name(), d.get(ATT_LOCKED), new HashSet<String>());
            abstractDoors.add(ad);
        }

        List<CleanupBlock> blocks = (List<CleanupBlock>) s.getBlocks().values();
        List<MoveBlock> abstractBlocks = new ArrayList<MoveBlock>();
        for(CleanupBlock b : blocks){

            int bx = (int) b.get(ATT_X);
            int by = (int) b.get(ATT_Y);
            CleanupRoom blockInRoom = s.roomContainingPoint(bx, by);

            String blockInRegion;
            if(blockInRoom != null){
                blockInRegion = blockInRoom.name();
            }
            else{
                CleanupDoor blockInDoor = s.doorContainingPoint(bx, by);
                blockInRegion = blockInDoor.name();
            }

            MoveBlock ab  = new MoveBlock(b.name(), b.get(ATT_SHAPE), b.get(ATT_COLOR), blockInRegion);
            abstractBlocks.add(ab);
        }




        //now set room and door connections
        for(CleanupRoom r : rooms){

            int rl = (int) r.get(ATT_LEFT);
            int rr = (int) r.get(ATT_RIGHT);
            int rt = (int) r.get(ATT_TOP);
            int rb = (int) r.get(ATT_BOTTOM);

            MoveRoom rL1 = null;
            for(MoveRoom rL1Temp : abstractRooms){
                if(rL1Temp.name().equals(r.name())){
                    rL1 = rL1Temp;
                    break;
                }
            }


//            ObjectInstance ar = as.getObject(r.getName());

            for(CleanupDoor d : doors){

                int dl = (int) d.get(ATT_LEFT);
                int dr = (int) d.get(ATT_RIGHT);
                int dt = (int) d.get(ATT_TOP);
                int db = (int) d.get(ATT_BOTTOM);

                if(rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr)){
                    MoveDoor dL1 = null;
                    for(MoveDoor dL1Temp : abstractDoors){

                        if(dL1Temp.name().equals(d.name())){
                            dL1 = dL1Temp;
                            dL1.connectedRegions.add(rL1.name());
                            break;
                        }
                    }
                    rL1.connectedRegions.add(dL1.name());
                }

            }

        }
        return new MoveState(abstractAgent, abstractBlocks, abstractDoors, abstractRooms);
    }

    protected static boolean rectanglesIntersect(int t1, int l1, int b1, int r1, int t2, int l2, int b2, int r2){

        return t2 >= b1 && b2 <= t1 && r2 >= l1 && l2 <= r1;

    }
}
