package taxi;

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
    public static final String ATT_VAL_IN_TAXI =			"inTaxi"; // possible value for "location"
    public static final String ATT_VAL_NOT_IN_TAXI =		"notInTaxi";
    public static final String ATT_VAL_ON_ROAD =			"onRoad";
    public static final String ATT_COLOR =					"color";
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
    public static final String COLOR_BLACK = 				"black";
    public static final String COLOR_GRAY =					"gray";

    //action indexes
    public static final int IND_NORTH = 0;
    public static final int IND_EAST = 1;
    public static final int IND_SOUTH = 2;
    public static final int IND_WEST = 3;
    public static final int IND_PICKUP = 4;
    public static final int IND_PUTDOWN = 5;



}
