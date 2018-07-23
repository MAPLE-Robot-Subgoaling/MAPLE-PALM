package edu.umbc.cs.maple.taxi;

public class TaxiConstants {

    //actions
    public static final int NUM_MOVE_ACTIONS = 				4;
    public static final String ERROR = 						"ERROR";
    public static final String ACTION_NAV =					"nav";
    public static final String ACTION_GET = 				"get";
    public static final String ACTION_PUT = 				"put";
    public static final String ACTION_NORTH = 				"north";
    public static final String ACTION_EAST =				"east";
    public static final String ACTION_SOUTH =				"south";
    public static final String ACTION_WEST = 				"west";
    public static final String ACTION_PICKUP = 				"pickup";
    public static final String ACTION_PUTDOWN = 			"putdown";

    public static final String CLASS_TAXI = 				"Taxi";
    public static final String CLASS_PASSENGER =			"Passenger";
    public static final String CLASS_LOCATION = 			"Location";
    public static final String CLASS_WALL = 				"Wall";

    public static final String ATT_X =						"x";
    public static final String ATT_Y =						"y";
    public static final String ATT_LOCATION =				"location";
    public static final String ATT_GOAL_LOCATION = 			"goalLocation";
    public static final String ATT_IN_TAXI = 				"inTaxi";
    public static final String ATT_COLOR =					"color";

    // possible value for "location"
    public static final String ATT_VAL_IN_TAXI =			"inTaxi";
    public static final String ATT_VAL_NOT_IN_TAXI =		"notInTaxi";
    public static final String ATT_VAL_ON_ROAD =			"onRoad";

    //wall attributes
    public static final String ATT_START_X = 				"startX";
    public static final String ATT_START_Y = 				"startY";
    public static final String ATT_LENGTH = 				"length";
    public static final String ATT_IS_HORIZONTAL =			"isHorizontal";

    //colors
    public static final String COLOR_RED = 					"red";
    public static final String COLOR_YELLOW = 				"yellow";
    public static final String COLOR_GREEN = 				"green";
    public static final String COLOR_BLUE = 				"blue";
    public static final String COLOR_MAGENTA =				"magenta";
    public static final String COLOR_PINK =                 "pink";
    public static final String COLOR_ORANGE =               "orange";
    public static final String COLOR_CYAN =                 "cyan";
    public static final String COLOR_BLACK = 				"black";
    public static final String COLOR_GRAY =					"gray";
    public static final String[] COLORS = {
            COLOR_RED, COLOR_YELLOW, COLOR_GREEN, COLOR_BLUE, COLOR_MAGENTA, COLOR_PINK,
            COLOR_ORANGE, COLOR_CYAN, COLOR_BLACK, COLOR_GRAY
    };

    //action indexes
    public static final int IND_NORTH = 0;
    public static final int IND_EAST = 1;
    public static final int IND_SOUTH = 2;
    public static final int IND_WEST = 3;
    public static final int IND_PICKUP = 4;
    public static final int IND_PUTDOWN = 5;

    //hiergen related
    public static final String ACTION_TASK_5 = "task5";
    public static final String ACTION_TASK_7 = "task7";
    public static final String ATT_DESTINATION_X = "destX";
    public static final String ATT_DESTINATION_Y = "destY";
    public static final String ATT_READY = "ready";
    public static final String PF_TASK_5 = "task5completed";
    public static final String PF_TASK_7 = "task7completed";



}
