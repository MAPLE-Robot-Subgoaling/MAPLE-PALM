
/*************************************************

	TAXI DOMAIN
		Neville Mehta

**************************************************/


#include <algorithm>
#include "taxi.h"



Taxi_State::Taxi_State (const Coordinate& map_size, const int& num_passengers, const int& taxi_capacity, const int& fuel_max)
								: map_size(map_size), num_passengers(num_passengers), taxi_capacity(taxi_capacity), fuel_max(fuel_max)
{	num_variables = num_taxi_variables + num_passengers * num_passenger_variables;
	num_sites = 4 + (fuel_max > 0);
	site_location.resize(num_sites);
	site_location[Red] = Coordinate(0, map_size.y - 1);
	site_location[Green] = Coordinate(map_size.x - 1, map_size.y - 1);
	site_location[Blue] = Coordinate(3 * map_size.x / 5, 0);
	site_location[Yellow] = Coordinate(0, 0);
	if (fuel_max > 0)
		site_location[Station] = Coordinate(2 * map_size.x / 5, 0);

	taxi.location = Coordinate(NA);
	taxi.fuel = NA;

	passengers.resize(num_passengers);
	for (auto& passenger : passengers)
	{	passenger.location = passenger.destination = Coordinate(NA);
		passenger.in_taxi = NA;
	}
}


vector<int> Taxi_State::variables () const
{	vector<int> state_variables;
	for (int v = 0; v < num_variables; ++v)
		if (variable_name(v) != "agent_fuel" || fuel_max > 0)
			state_variables.push_back(v);
	return state_variables;
}


// Variable name to integer index
int Taxi_State::variable_index (const string& variable) const
{	if (variable == "agent_x")
		return 0;
	if (variable == "agent_y")
		return 1;
	if (variable == "agent_fuel")
		return 2;

	if (variable.substr(0, 9) == "passenger")
	{	int passenger_id;
		if (num_passengers == 1)
			passenger_id = 0;   // In case the single passenger isn't tagged with the id
		else
			passenger_id = from_string<int>(variable.substr(9, variable.find_first_of("_", 9) - 9));

		if (passenger_id >= 0 && passenger_id < num_passengers)
		{	if (variable.substr(variable.find_first_of("_")) == "_location_x")
				return num_taxi_variables + passenger_id * num_passenger_variables + 0;
			if (variable.substr(variable.find_first_of("_")) == "_location_y")
				return num_taxi_variables + passenger_id * num_passenger_variables + 1;
			if (variable.substr(variable.find_first_of("_")) == "_destination_x")
				return num_taxi_variables + passenger_id * num_passenger_variables + 2;
			if (variable.substr(variable.find_first_of("_")) == "_destination_y")
				return num_taxi_variables + passenger_id * num_passenger_variables + 3;
			if (variable.substr(variable.find_first_of("_")) == "_in_taxi")
				return num_taxi_variables + passenger_id * num_passenger_variables + 4;
			if (variable.substr(variable.find_first_of("_")) == "_location")
				return num_variables + passenger_id * num_passenger_accessors;
			if (variable.substr(variable.find_first_of("_")) == "_destination")
				return num_variables + passenger_id * num_passenger_accessors + 1;
		}
	}

	throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
}


string Taxi_State::variable_name (const int& variable_index) const
{	int variable = variable_index;
	switch (variable)
	{	case 0:
			return "agent_x";
		case 1:
			return "agent_y";
		case 2:
			return "agent_fuel";
		default:
			if (variable < num_variables)
			{	variable -= num_taxi_variables;
				switch (variable % num_passenger_variables)
				{	case 0:
						return "passenger" + (num_passengers > 1 ? to_string(variable / num_passenger_variables) : "") + "_location_x";
					case 1:
						return "passenger" + (num_passengers > 1 ? to_string(variable / num_passenger_variables) : "") + "_location_y";
					case 2:
						return "passenger" + (num_passengers > 1 ? to_string(variable / num_passenger_variables) : "") + "_destination_x";
					case 3:
						return "passenger" + (num_passengers > 1 ? to_string(variable / num_passenger_variables) : "") + "_destination_y";
					case 4:
						return "passenger" + (num_passengers > 1 ? to_string(variable / num_passenger_variables) : "") + "_in_taxi";
					default:
						throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
				}
			}
			else if (variable < num_variables + num_passengers * num_passenger_accessors)
			{	variable -= num_variables;
				switch (variable % num_passenger_accessors)
				{	case 0:
						return "passenger" + (num_passengers > 1 ? to_string(variable / num_passenger_accessors) : "") + "_location";
					case 1:
						return "passenger" + (num_passengers > 1 ? to_string(variable / num_passenger_accessors) : "") + "_destination";
					default:
						throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
				}
			}
			else
				throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


int Taxi_State::variable_size (const int& variable_index) const
{	int variable = variable_index;
	switch (variable)
	{	case 0:   // agent_x
			return map_size.x;
		case 1:   // agent_y
			return map_size.y;
		case 2:   // agent_fuel
			return fuel_max;

		default:
			if (variable < num_variables)
			{	variable -= num_taxi_variables;
				switch (variable % num_passenger_variables)
				{	case 0:   // passenger_location_x
						return map_size.x;
					case 1:   // passenger_location_y
						return map_size.y;
					case 2:   // passenger_destination_x
						return map_size.x;
					case 3:   // passenger_destination_y
						return map_size.y;
					case 4:   // passenger_in_taxi
						return 2;
					default:
						throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
				}
			}
			else if (variable < num_variables + num_passengers * num_passenger_accessors)
			{	variable -= num_variables;
				switch (variable % num_passenger_accessors)
				{	case 0:   // passenger_location
						return num_sites;
					case 1:   // passenger_destination
						return num_sites;
					default:
						throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
				}
			}
			else
				throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}

int Taxi_State::debug_num_states() const 
{
	throw HierException(__FILE__, __LINE__, "ERROR: debug num states is being used (needed for Cleanup)");
}

int Taxi_State::variable_size_for_state (const int& variable_index) const
{
	return variable_size(variable_index);
}

int Taxi_State::variable (const int& variable_index) const
{	int variable = variable_index;
	switch (variable)
	{	case 0:
			return taxi.location.x;
		case 1:
			return taxi.location.y;
		case 2:
			return taxi.fuel;
		default:
			if (variable < num_variables)
			{	variable -= num_taxi_variables;
				switch (variable % num_passenger_variables)
				{	case 0:   // passenger_location_x
						return passengers[variable / num_passenger_variables].location.x;
					case 1:   // passenger_location_y
						return passengers[variable / num_passenger_variables].location.y;
					case 2:   // passenger_destination_x
						return passengers[variable / num_passenger_variables].destination.x;
					case 3:   // passenger_destination_y
						return passengers[variable / num_passenger_variables].destination.y;
					case 4:   // passenger_in_taxi
						return passengers[variable / num_passenger_variables].in_taxi;
					default:
						throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
				}
			}
			else if (variable < num_variables + num_passengers * num_passenger_accessors)
			{	variable -= num_variables;
				switch (variable % num_passenger_accessors)
				{	case 0:   // passenger_location
						for (int loc = 0; loc < num_sites; ++loc)
							if (passengers[variable / num_passenger_accessors].location == site_location[loc])
								return loc;
						return In_taxi;
					case 1:   // passenger_destination
						for (int loc = 0; loc < num_sites; ++loc)
							if (passengers[variable / num_passenger_accessors].destination == site_location[loc])
								return loc;
					default:
						throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
				}
			}
			else
				throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


int& Taxi_State::variable (const int& variable_index)
{	int variable = variable_index;
	switch (variable)
	{	case 0:
			return taxi.location.x;
		case 1:
			return taxi.location.y;
		case 2:
			return taxi.fuel;
		default:
			if (variable < num_variables)
			{	variable -= num_taxi_variables;
				switch (variable % num_passenger_variables)
				{	case 0:   // passenger_location_x
						return passengers[variable / num_passenger_variables].location.x;
					case 1:   // passenger_location_y
						return passengers[variable / num_passenger_variables].location.y;
					case 2:   // passenger_destination_x
						return passengers[variable / num_passenger_variables].destination.x;
					case 3:   // passenger_destination_y
						return passengers[variable / num_passenger_variables].destination.y;
					case 4:   // passenger_in_taxi
						return passengers[variable / num_passenger_variables].in_taxi;
					default:
						throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
				}
			}
			else
				throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


map<int,int> Taxi_State::variables_mapper () const
{	map<int,int> var_map;
	for (int i = 0; i < num_variables; ++i)
		if (variable(i) != NA)
			var_map[i] = variable(i);
	return var_map;
}


pair<bool,int> Taxi_State::parse (string expression) const
{	expression = replace(expression, " ", "");
	if (expression == "")
		throw HierException(__FILE__, __LINE__, "Expression string is empty.");

	int total = 0;
	expression.insert(0, "+");
	size_t start = expression.find_first_not_of("+-", 0);
	size_t end = expression.find_first_of("+-", start);
	while (start != string::npos || end != string::npos)
	{	istringstream token(expression.substr(start, end - start));
		int value;
		if (!(token >> value))   // If 'token' is not an integer
		{	if (token.str() == "NA")
				value = NA;
			else if (token.str() == "NL")
				value = NL;
			else if (token.str() == "Red")
				value = Red;
			else if (token.str() == "Green")
				value = Green;
			else if (token.str() == "Blue")
				value = Blue;
			else if (token.str() == "Yellow")
				value = Yellow;
			else if (token.str() == "Station")
				value = Station;
			else if (token.str() == "num_sites")
				value = num_sites;
			else if (token.str() == "map_size.x")
				value = map_size.x;
			else if (token.str() == "map_size.y")
				value = map_size.y;
			else
				return make_pair(false, 0);
				//throw HierException(__FILE__, __LINE__, "Cannot parse " + expression);
		}
		total += (expression[start-1] == '+') ? value : -value;

		start = expression.find_first_not_of("+-", end);
		end = expression.find_first_of("+-", start);
	}

	return make_pair(true, total);
}


void Taxi_State::read (istream& in)
{	if (in >> taxi.location)
	{	if (fuel_max > 0)
		{	string reader;
			if (in >> reader)
				taxi.fuel = (reader == "*") ? Taxi_State::NA : from_string<int>(reader);
			else
				throw HierException(__FILE__, __LINE__, "Bad state read.");
		}

		for (auto& passenger : passengers)
		{	if (in >> passenger.location)
			{	in >> passenger.destination;
				in >> passenger.in_taxi;
			}
			else
				throw HierException(__FILE__, __LINE__, "Bad state read.");
		}
	}
}


string Taxi_State::print () const
{	ostringstream out;
	
	out << "(";
	if (taxi.location.x != NA)
		out << taxi.location.x;
	else
		out << "*";
	out << ",";
	if (taxi.location.y != NA)
		out << taxi.location.y;
	else
		out << "*";
	out << ")";

	if (fuel_max > 0)
	{	if (taxi.fuel != NA)
			out << " " << taxi.fuel;
		else
			out << " *";
	}

	for (const auto& passenger : passengers)
	{	out << " (";
		if (passenger.location.x != NA)
			out << passenger.location.x;
		else
			out << "*";
		out << ",";
		if (passenger.location.y != NA)
			out << passenger.location.y;
		else
			out << "*";
		out << ")";

		out << " (";
		if (passenger.destination.x != NA)
			out << passenger.destination.x;
		else
			out << "*";
		out << ",";
		if (passenger.destination.y != NA)
			out << passenger.destination.y;
		else
			out << "*";
		out << ")";

		if (passenger.in_taxi != NA)
			out << " " << passenger.in_taxi;
		else
			out << " *";
	}

	return out.str();
}

//*************************************************************************************************

Taxi::Taxi (const string& name, const double& success_probability) : MDP(name, new Taxi_State()), reward_default(-1), reward_dropoff(20.0), reward_illegal(-10.0),
																												Pr_successful_execution(success_probability), noisy(false)
{	map_creator();
	initialize();
}


void Taxi::map_creator (const int& mode)
{	map_walls.resize(2 * state().map_size.x + 1, 2 * state().map_size.y + 1);
	map_walls = false;
	for (int x = 0; x < 2 * state().map_size.x + 1; ++x)
	{	for (int y = 0; y < 2 * state().map_size.y + 1; ++y)
		{	// Inner walls

			// Original taxi world
			if ((y < state().map_size.y && (x == 2 * (state().map_size.x / 5) || x == 2 * (3 * state().map_size.x / 5)))
						|| (y > state().map_size.y && x == 2 * (2 * state().map_size.x / 5)))
				map_walls(x,y) = true;

			// Corridors
			//if (mode == 0)
			//{	if (y % 2 == 0 && x != state().map_size.x)
			//		map_walls(x,y) = true;
			//	else
			//		map_walls(x,y) = false;
			//}
			//else
			//{	if (x % 2 == 0 && y != state().map_size.y)
			//		map_walls(x,y) = true;
			//	else
			//		map_walls(x,y) = false;
			//}

			// Serpentine
			//if (mode == 0)
			//{	if ((y % 4 == 0 && x > 0 && x < 2 * state().map_size.x - 1) || (y % 4 == 2 && x > 1 && x < 2 * state().map_size.x + 1))
			//		map_walls(x,y) = true;
			//	else
			//		map_walls(x,y) = false;
			//}
			//else
			//{	if ((x % 4 == 0 && y > 0 && y < 2 * state().map_size.y - 1) || (x % 4 == 2 && y > 1 && y < 2 * state().map_size.y + 1))
			//		map_walls(x,y) = true;
			//	else
			//		map_walls(x,y) = false;
			//}

			// Outer walls
			if (x == 0 || x == 2 * state().map_size.x || y == 0 || y == 2 * state().map_size.y)
				map_walls(x,y) = true;
		}
	}

/*	cout << endl;
	for (int y = 2 * state().map_size.y; y >= 0; --y)
	{	for (int x = 0; x < 2 * state().map_size.x + 1; ++x)
		{	if (x % 2 && y % 2)
				cout << "  ";
			else
				cout << (map_walls(x,y) ? "#" : (y % 2 ? "|" : "-")) << " ";
		}
		cout << endl;
	}
	cout << endl;
*/
}


void Taxi::initialize (const bool& target)
{	state().taxi.location = state().site_location[rand_int(state().num_sites)];
	state().taxi.fuel = state().fuel_max > 0 ? state().fuel_max : Taxi_State::NA;

	for (auto& passenger : state().passengers)
	{	int special_location = rand_int(state().num_sites);
		passenger.location = state().site_location[special_location];

		// Passenger's destination never equals its source, except when dropped off (signaling the goal state)
		passenger.destination = state().site_location[(special_location + 1 + rand_int(state().num_sites - 1)) % state().num_sites];
		passenger.in_taxi = false;
	}

	_reward = 0.0;
	_duration = 0.0;
}


void Taxi::process (const vector<int>& action)
{	_reward = reward_default;
	_duration = 1.0;

	switch (action[0])
	{	case North:
			if (rand_real() < Pr_successful_execution)
			{	if (!map_walls(2 * state().taxi.location.x + 1, 2 * state().taxi.location.y + 2))
					++state().taxi.location.y;
			}
			else if (noisy)
			{	if (rand_int(2))
				{	if (!map_walls(2 * state().taxi.location.x, 2 * state().taxi.location.y + 1))
						--state().taxi.location.x;
				}
				else if (!map_walls(2 * state().taxi.location.x + 2, 2 * state().taxi.location.y + 1))
					++state().taxi.location.x;
			}
			break;

		case South:
			if (rand_real() < Pr_successful_execution)
			{	if (!map_walls(2 * state().taxi.location.x + 1, 2 * state().taxi.location.y))
					--state().taxi.location.y;
			}
			else if (noisy)
			{	if (rand_int(2))
				{	if (!map_walls(2 * state().taxi.location.x + 2, 2 * state().taxi.location.y + 1))
						++state().taxi.location.x;
				}
				else if (!map_walls(2 * state().taxi.location.x, 2 * state().taxi.location.y + 1))
					--state().taxi.location.x;
			}
			break;

		case East:
			if (rand_real() < Pr_successful_execution)
			{	if (!map_walls(2 * state().taxi.location.x + 2, 2 * state().taxi.location.y + 1))
					++state().taxi.location.x;
			}
			else if (noisy)
			{	if (rand_int(2))
				{	if (!map_walls(2 * state().taxi.location.x + 1, 2 * state().taxi.location.y + 2))
						++state().taxi.location.y;
				}
				else if (!map_walls(2 * state().taxi.location.x + 1, 2 * state().taxi.location.y))
					--state().taxi.location.y;
			}
			break;

		case West:
			if (rand_real() < Pr_successful_execution)
			{	if (!map_walls(2 * state().taxi.location.x, 2 * state().taxi.location.y + 1))
					--state().taxi.location.x;
			}
			else if (noisy)
			{	if (rand_int(2))
				{	if (!map_walls(2 * state().taxi.location.x + 1, 2 * state().taxi.location.y))
						--state().taxi.location.y;
				}
				else if (!map_walls(2 * state().taxi.location.x + 1, 2 * state().taxi.location.y + 2))
					++state().taxi.location.y;
			}
			break;

		case Pickup:
			if (rand_real() < Pr_successful_execution)
			{	int passengers_in_taxi = 0;
				for (const auto& passenger : state().passengers)
					if (passenger.in_taxi)
						++passengers_in_taxi;

				bool pickup_failure = true;
				for (int p = 0; p < state().num_passengers && passengers_in_taxi < state().taxi_capacity; ++p)
					if (state().taxi.location == state().passengers[p].location)   // Taxi (not full) at a passenger's source location
					{	state().passengers[p].in_taxi = true;
						pickup_failure = false;
						++passengers_in_taxi;
					}
				if (pickup_failure)
					_reward += reward_illegal;
			}
			break;

		case Dropoff:
			if (rand_real() < Pr_successful_execution)
			{	bool dropoff_failure = true;
				for (auto& passenger : state().passengers)
					if (passenger.in_taxi && state().taxi.location == passenger.destination)   // Taxi's at its passenger's destination
					{	passenger.in_taxi = false;
						passenger.location = state().taxi.location;   // Drop off the passenger
						_reward += reward_dropoff;
						dropoff_failure = false;
					}
				if (dropoff_failure)
					_reward += reward_illegal;
			}
			break;

		case Refuel:
			if (rand_real() < Pr_successful_execution)
				if (state().taxi.location == state().site_location[Taxi_State::Station])
					state().taxi.fuel = state().fuel_max;
			break;

		case Wait:
			break;

		default:
			throw HierException(__FILE__, __LINE__, "Unknown action.");
	}

	if (state().taxi.fuel > 0)
		--state().taxi.fuel;
}


bool Taxi::terminated () const
{	if (state().taxi.fuel == 0)
		return true;

	for (const auto& passenger : state().passengers)
		if (passenger.in_taxi || passenger.location != passenger.destination)
			return false;
	return true;
}


int Taxi::action_index (const string& action_name) const
{	if (action_name == "North")
		return North;
	if (action_name == "South")
		return South;
	if (action_name == "East")
		return East;
	if (action_name == "West")
		return West;
	if (action_name == "Pickup")
		return Pickup;
	if (action_name == "Dropoff")
		return Dropoff;
	if (action_name == "Refuel")
		return Refuel;
	if (action_name == "Wait")
		return Wait;
	throw HierException(__FILE__, __LINE__, "Unknown action name.");
}


string Taxi::print_action (const int& action) const
{	switch (action)
	{	case North:
			return "North";
		case South:
			return "South";
		case East:
			return "East";
		case West:
			return "West";
		case Pickup:
			return "Pickup";
		case Dropoff:
			return "Dropoff";
		case Refuel:
			return "Refuel";
		case Wait:
			return "Wait";
		default:
			throw HierException(__FILE__, __LINE__, "Unknown action.");
	}
}
