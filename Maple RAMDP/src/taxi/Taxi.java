package taxi;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;

public class Taxi implements DomainGenerator{

	//public constants for general use
	//object classes
	public static final String CLASS_TAXI = 				"Taxi";
	public static final String CLASS_PASSENGER =			"Passenger";
	public static final String CLASS_LOCATION = 			"Location";
	public static final String CLASS_WALL = 				"Wall";
	
	//common attributes
	public static final String ATT_X =						"x";
	public static final String ATT_Y =						"y";
	
	//taxi attributes
	public static final String ATT_TAXI_OCCUPIED = 			"taxiOccupied";
	
	//passenger attributes
	public static final String ATT_GOAL_LOCATION = 			"goalLocation";
	public static final String ATT_IN_TAXI = 				"inTaxi";
	public static final String ATT_JUST_PICKED_UP = 		"justPickedUp";
	
	//location attributes 
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
	public static final String COLOR_GLUE = 				"blue";
	
	
	@Override
	public Domain generateDomain() {
		return null;
	}

}
