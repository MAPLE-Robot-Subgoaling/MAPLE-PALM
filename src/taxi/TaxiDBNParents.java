package taxi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.state.State;

public class TaxiDBNParents {

	public static Map<String, Map<String, List<String>>> getParents(State s){
		Map<String, Map<String, List<String>>> parents = new HashMap<String, Map<String, List<String>>>();
		List<String> pars;
		
		parents.put(Taxi.ACTION_PICKUP, new HashMap<String, List<String>>());
		pars = new ArrayList<>();
		parents.get(Taxi.ACTION_PICKUP).put("R", pars);
		for(Object v : s.variableKeys()){
			pars = new ArrayList<String>();
			parents.get(Taxi.ACTION_PICKUP).put(v.toString(), pars);
			String varprime = v.toString();
			
			for(Object vp : s.variableKeys()){
				String var = vp.toString();
				
				if(varprime.startsWith(Taxi.CLASS_TAXI)){
					if(varprime.endsWith(Taxi.ATT_X)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_Y)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_TAXI_OCCUPIED)){
						if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_TAXI_OCCUPIED) 
								|| var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_PASSENGER) && (var.endsWith(Taxi.ATT_X)
								|| var.endsWith(Taxi.ATT_Y) || var.endsWith(Taxi.ATT_IN_TAXI)))
							pars.add(var);
					}
				}else if(varprime.startsWith(Taxi.CLASS_PASSENGER)){
					String name = varprime.substring(0, varprime.indexOf(":"));
					if(varprime.endsWith(Taxi.ATT_X)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_Y)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_IN_TAXI)){
						if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) 
								 || var.endsWith(Taxi.ATT_Y) || var.endsWith(Taxi.ATT_TAXI_OCCUPIED)))
							pars.add(var);
						if(var.startsWith(name) && ( var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)
								|| var.endsWith(Taxi.ATT_IN_TAXI)))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_GOAL_LOCATION)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_PICKED_UP_AT_LEAST_ONCE)){
						if(var.equals(varprime))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) 
								 || var.endsWith(Taxi.ATT_Y) || var.endsWith(Taxi.ATT_TAXI_OCCUPIED)))
							pars.add(var);
						if(var.startsWith(name) && ( var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)
								|| var.endsWith(Taxi.ATT_IN_TAXI)))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_JUST_PICKED_UP)){
						if(var.equals(varprime))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) 
								 || var.endsWith(Taxi.ATT_Y) || var.endsWith(Taxi.ATT_TAXI_OCCUPIED)))
							pars.add(var);
						if(var.startsWith(name) && ( var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)
								|| var.endsWith(Taxi.ATT_IN_TAXI)))
							pars.add(var);
					}
				}else if(varprime.startsWith(Taxi.CLASS_LOCATION)){
					if(var.equals(varprime))
						pars.add(var);
				}else if(varprime.startsWith(Taxi.CLASS_WALL)){
					if(var.equals(varprime))
						pars.add(var);
				}
			}
			
			pars = parents.get(Taxi.ACTION_PICKUP).get("R");
			if(varprime.startsWith(Taxi.CLASS_TAXI) && (varprime.endsWith(Taxi.ATT_TAXI_OCCUPIED) 
					|| varprime.endsWith(Taxi.ATT_X) || varprime.endsWith(Taxi.ATT_Y)))
				pars.add(varprime);
			if(varprime.startsWith(Taxi.CLASS_PASSENGER) && (varprime.endsWith(Taxi.ATT_X)
					|| varprime.endsWith(Taxi.ATT_Y) || varprime.endsWith(Taxi.ATT_IN_TAXI)))
				pars.add(varprime);
		}
		
		parents.put(Taxi.ACTION_DROPOFF, new HashMap<String, List<String>>());
		pars = new ArrayList<>();
		parents.get(Taxi.ACTION_DROPOFF).put("R", pars);
		
		for(Object v : s.variableKeys()){
			pars = new ArrayList<String>();
			parents.get(Taxi.ACTION_DROPOFF).put(v.toString(), pars);
			String varprime = v.toString();
			
			for(Object vp : s.variableKeys()){
				String var = vp.toString();
				
				if(varprime.startsWith(Taxi.CLASS_TAXI)){
					if(varprime.endsWith(Taxi.ATT_X)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_Y)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_TAXI_OCCUPIED)){
						if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_TAXI_OCCUPIED) 
								|| var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_PASSENGER) && var.endsWith(Taxi.ATT_IN_TAXI))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_LOCATION) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
							pars.add(var);
					}
				}else if(varprime.startsWith(Taxi.CLASS_PASSENGER)){
					String name = varprime.substring(0, varprime.indexOf(":"));
					if(varprime.endsWith(Taxi.ATT_X)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_Y)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_IN_TAXI)){
						if(var.equals(varprime))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_LOCATION) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_GOAL_LOCATION)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_PICKED_UP_AT_LEAST_ONCE)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.endsWith(Taxi.ATT_JUST_PICKED_UP)){
						if(var.equals(varprime))
							pars.add(var);
						if(var.startsWith(name) && var.endsWith(Taxi.ATT_IN_TAXI))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
							pars.add(var);
						if(var.startsWith(Taxi.CLASS_LOCATION) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
							pars.add(var);
					}
				}else if(varprime.startsWith(Taxi.CLASS_LOCATION)){
					if(var.equals(varprime))
						pars.add(var);
				}else if(varprime.startsWith(Taxi.CLASS_WALL)){
					if(var.equals(varprime))
						pars.add(var);
				}
			}
			
			pars = parents.get(Taxi.ACTION_DROPOFF).get("R");
			if(varprime.startsWith(Taxi.CLASS_LOCATION) && (varprime.endsWith(Taxi.ATT_COLOR) 
					|| varprime.endsWith(Taxi.ATT_X) || varprime.endsWith(Taxi.ATT_Y)))
				pars.add(varprime);
			if(varprime.startsWith(Taxi.CLASS_PASSENGER) && (varprime.endsWith(Taxi.ATT_X)
					|| varprime.endsWith(Taxi.ATT_Y) || varprime.endsWith(Taxi.ATT_IN_TAXI) 
					|| varprime.endsWith(Taxi.ATT_GOAL_LOCATION)))
				pars.add(varprime);
		}
		String[] movement = {Taxi.ACTION_NORTH, Taxi.ACTION_EAST, Taxi.ACTION_SOUTH, Taxi.ACTION_WEST};
		for(String a : movement){
			parents.put(a, new HashMap<String, List<String>>());
			pars = new ArrayList<>();
			parents.get(a).put("R", pars);
			for(Object v : s.variableKeys()){
				pars = new ArrayList<String>();
				parents.get(a).put(v.toString(), pars);
				String varprime = v.toString();
				
				for(Object vp : s.variableKeys()){
					String var = vp.toString();
					
					if(varprime.startsWith(Taxi.CLASS_TAXI)){
						if(varprime.endsWith(Taxi.ATT_X)){
							if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
								pars.add(var);
						}else if(varprime.endsWith(Taxi.ATT_Y)){
							if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
								pars.add(var);
						}else if(varprime.endsWith(Taxi.ATT_TAXI_OCCUPIED)){
							if(var.equals(varprime))
								pars.add(var);
						}
					}else if(varprime.startsWith(Taxi.CLASS_PASSENGER)){
						String name = varprime.substring(0, varprime.indexOf(":"));
						if(varprime.endsWith(Taxi.ATT_X)){
							if(var.equals(varprime))
								pars.add(var);
							if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
								pars.add(var);
						}else if(varprime.endsWith(Taxi.ATT_Y)){
							if(var.equals(varprime))
								pars.add(var);
							if(var.startsWith(Taxi.CLASS_TAXI) && (var.endsWith(Taxi.ATT_X) || var.endsWith(Taxi.ATT_Y)))
								pars.add(var);
						}else if(varprime.endsWith(Taxi.ATT_IN_TAXI)){
							if(var.equals(varprime))
								pars.add(var);
						}else if(varprime.endsWith(Taxi.ATT_GOAL_LOCATION)){
							if(var.equals(varprime))
								pars.add(var);
						}else if(varprime.endsWith(Taxi.ATT_PICKED_UP_AT_LEAST_ONCE)){
							if(var.equals(varprime))
								pars.add(var);
						}else if(varprime.endsWith(Taxi.ATT_JUST_PICKED_UP)){
							if(var.equals(varprime))
								pars.add(var);
							if(var.startsWith(name) && var.endsWith(Taxi.ATT_IN_TAXI))
								pars.add(var);
						}
					}else if(varprime.startsWith(Taxi.CLASS_LOCATION)){
						if(var.equals(varprime))
							pars.add(var);
					}else if(varprime.startsWith(Taxi.CLASS_WALL)){
						if(var.equals(varprime))
							pars.add(var);
					}
				}
			}
		}
		
		for(String a : parents.keySet()){
			System.out.println(a);
			for(String v : parents.get(a).keySet()){
				System.out.println(v + " " + parents.get(a).get(v).toString());
			}
			System.out.println();
		}
		return parents;
	}
}
