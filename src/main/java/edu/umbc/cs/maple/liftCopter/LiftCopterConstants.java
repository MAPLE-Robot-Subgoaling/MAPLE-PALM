package edu.umbc.cs.maple.liftCopter;

public class LiftCopterConstants {
    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_CARGO = "cargo";
    public static final String CLASS_LOCATION = "depot";
    public static final String CLASS_WALL = "Wall";


    public static final int NUM_MOVE_ACTIONS = 5;
    public static final String ERROR = "ERROR";
    public static final String ACTION_NAV = "nav";
    public static final String ACTION_GET = "get";
    public static final String ACTION_PUT = "put";
    public static final String ACTION_THRUST_NORTH = "tn";
    public static final String ACTION_THRUST_SOUTH = "ts";
    public static final String ACTION_THRUST_EAST = "te";
    public static final String ACTION_THRUST_WEST = "tw";
    public static final String ACTION_THRUST = "thrust";
    public static final String ACTION_IDLE = "noop";
    public static final String ACTION_PICKUP = "pickup";
    public static final String ACTION_PUTDOWN = "putdown";

    public static final String ATT_X = "x";
    public static final String ATT_Y = "y";
    public static final String ATT_W = "width";
    public static final String ATT_H = "height";
    public static final String ATT_VX = "vx";
    public static final String ATT_VY = "vy";
    public static final String ATT_LOCATION = "location";
    public static final String ATT_GOAL_LOCATION = "goalLocation";
    public static final String ATT_PICKED_UP = "pickedUp";
    public static final String ATT_COLOR = "color";

    public static final String ATT_VAL_PICKED_UP = "pickedUp";
    public static final String ATT_VAL_IN_AIR = "flyin";


    //wall attributes
    public static final String ATT_START_X = "startX";
    public static final String ATT_START_Y = "startY";
    public static final String ATT_HEIGHT = "height";
    public static final String ATT_WIDTH = "width";

    //colors
    public static final String COLOR_RED = "red";
    public static final String COLOR_YELLOW = "yellow";
    public static final String COLOR_GREEN = "green";
    public static final String COLOR_BLUE = "blue";
    public static final String COLOR_MAGENTA = "magenta";
    public static final String COLOR_BLACK = "black";
    public static final String COLOR_GRAY = "gray";

    //action indexes
    public static final int IND_THRUST = 3;
    public static final int IND_IDLE = 4;
    public static final int IND_PICKUP = 5;
    public static final int IND_PUTDOWN = 6;

    public static final String PF_OVER_CARGO = "overCargo";
    public static final String PF_OVER_DEPOT = "overDepot";




    public static final double PHYS_MAX_VX = 4;
    public static final double PHYS_MAX_VY = 4;


}
