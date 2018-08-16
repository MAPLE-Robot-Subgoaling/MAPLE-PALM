
/*************************************************

	CLEANUP DOMAIN
		John Winder, based on Mehta's Taxi and Burlap's Cleanup domain

**************************************************/


#include <algorithm>
#include "cleanup.h"

Cleanup_State::Cleanup_State (
	const Coordinate& map_size,
	const int& num_blocks,
	const int& num_doors,
	const int& num_rooms)
		: map_size(map_size), num_blocks(num_blocks), num_doors(num_doors), num_rooms(num_rooms)
{	
	num_variables = num_agent_variables + num_blocks * num_block_variables + num_doors * num_door_variables + num_rooms * num_room_variables;
	blocks.resize(num_blocks);
	doors.resize(num_doors);
	rooms.resize(num_rooms);
}


vector<int> Cleanup_State::variables () const
{	
	vector<int> state_variables;
	for (int v = 0; v < num_variables; ++v)
		state_variables.push_back(v);
	return state_variables;
}


// Variable name to integer index
int Cleanup_State::variable_index (const string& variable) const
{	
	int id;
	int underscore;
	int variable_index = 0;
	for (int j = 0; j < num_state_classes; ++j) {
		string state_class_test = state_classes[j];
		int state_class_name_length = state_classes_name_lengths[j];
		string state_class_name = variable.substr(0, state_class_name_length);
		if (state_class_name == state_class_test) {
			underscore = variable.find_first_of("_", state_class_name_length);
			id = from_string<int>(variable.substr(state_class_name_length, underscore - state_class_name_length));
			for (int i = 0; i < sizeof(agent_variables) / sizeof(agent_variables[0]); ++i) {
				string variable_name = variable.substr(underscore+1);
				string variable_test = agent_variables[i];
				variable_index += 1;
				if (variable_name == variable_test) {
					return variable_index;
				}
			}
			throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
		} else {
			variable_index += state_classes_num_variables[j];
		}
	}
	throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);

	/*if ( == "agent") {
	} else if (variable.substr(0, block_length) == "block") {
		length = block_length;
		underscore = variable.find_first_of("_", length);
		id = from_string<int>(variable.substr(length, underscore - length));
	} else if (variable.substr(0, door_length) == "door") {
		length = door_length;
		underscore = variable.find_first_of("_", length);
		id = from_string<int>(variable.substr(length, underscore - length));
	} else if (variable.substr(0, room_length) == "room") {
		length = room_length;
		underscore = variable.find_first_of("_", length);
		id = from_string<int>(variable.substr(length, underscore - length));
	} else {
		throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}*/
}


string Cleanup_State::variable_name (const int& variable_index) const
{	int variable = variable_index;
	switch (variable)
	{	case 0:
			return "agent_x";
		case 1:
			return "agent_y";
		case 2:
			return "agent_fuel";
		default:
			throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


int Cleanup_State::variable_size (const int& variable_index) const
{	
	int variable = variable_index;
	switch (variable)
	{	case 0:   // agent_x
			return map_size.x;
		case 1:   // agent_y
			return map_size.y;
		default:
			throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


int Cleanup_State::variable (const int& variable_index) const
{	
	int variable = variable_index;
	switch (variable)
	{	case 0:
			return agent.location.x;
		case 1:
			return agent.location.y;
		default:
			throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


int& Cleanup_State::variable (const int& variable_index)
{	int variable = variable_index;
	switch (variable)
	{	case 0:
			return agent.location.x;
		case 1:
			return agent.location.y;
		default:
			throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


map<int,int> Cleanup_State::variables_mapper () const
{	map<int,int> var_map;
	for (int i = 0; i < num_variables; ++i)
		var_map[i] = variable(i);
	return var_map;
}


pair<bool,int> Cleanup_State::parse (string expression) const
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
		{
			if (token.str() == "red")
				value = red;
			else if (token.str() == "green")
				value = green;
			else if (token.str() == "blue")
				value = blue;
			else if (token.str() == "yellow")
				value = yellow;
			else if (token.str() == "map_size.x")
				value = map_size.x;
			else if (token.str() == "map_size.y")
				value = map_size.y;
			else
				//return make_pair(false, 0);
				throw HierException(__FILE__, __LINE__, "Cannot parse " + expression);
		}
		total += (expression[start-1] == '+') ? value : -value;

		start = expression.find_first_not_of("+-", end);
		end = expression.find_first_of("+-", start);
	}

	return make_pair(true, total);
}


void Cleanup_State::read (istream& in)
{
	throw HierException(__FILE__, __LINE__, "Bad state read.");
}


string Cleanup_State::print () const
{	
	ostringstream out;
	
	out << "(";
	out << agent.location.x;
	out << ",";
	out << agent.location.y;
	out << ")";

	for (const auto& block : blocks)
	{
		out << " (";
		out << block.location.x;
		out << ",";
		out << block.location.y;
		out << ")";
	}

	return out.str();
}

//*************************************************************************************************

Cleanup::Cleanup (const string& name, const double& success_probability) : MDP(name,
	new Cleanup_State()), reward_default(-1), reward_dropoff(20.0), reward_illegal(-10.0),
	Pr_successful_execution(success_probability), noisy(false)
{	
	map_creator();
	initialize();
}


void Cleanup::map_creator (const int& mode)
{	

}


void Cleanup::initialize (const bool& target)
{	

	// TODO: init the state 

	_reward = 0.0;
	_duration = 0.0;
}


void Cleanup::process (const vector<int>& action)
{	
	_reward = reward_default;
	_duration = 1.0;

	switch (action[0])
	{	case north:
			if (rand_real() < Pr_successful_execution)
			{	
				++state().agent.location.y;
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case south:
			if (rand_real() < Pr_successful_execution)
			{	
				--state().agent.location.y;
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case east:
			if (rand_real() < Pr_successful_execution)
			{	
				++state().agent.location.x;
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case west:
			if (rand_real() < Pr_successful_execution)
			{
				--state().agent.location.x;
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case pull:
			if (rand_real() < Pr_successful_execution)
			{ 
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		default:
			throw HierException(__FILE__, __LINE__, "Unknown action.");
	}

}


bool Cleanup::terminated () const
{	
	throw HierException(__FILE__, __LINE__, "terminated not implemented.");
}


int Cleanup::action_index (const string& action_name) const
{	if (action_name == "north")
		return north;
	if (action_name == "south")
		return south;
	if (action_name == "east")
		return east;
	if (action_name == "west")
		return west;
	if (action_name == "pull")
		return pull;
	throw HierException(__FILE__, __LINE__, "Unknown action name.");
}


string Cleanup::print_action (const int& action) const
{	switch (action)
	{	case north:
			return "north";
		case south:
			return "south";
		case east:
			return "east";
		case west:
			return "west";
		case pull:
			return "pull";
		default:
			throw HierException(__FILE__, __LINE__, "Unknown action.");
	}
}
			