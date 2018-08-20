
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
	num_variables = num_agent_variables_data + num_blocks * num_block_variables + num_doors * num_door_variables + num_rooms * num_room_variables;
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
		int state_class_num_variables = state_classes_num_variables[j];
		int state_class_name_length = state_classes_name_lengths[j];
		string state_class_name = variable.substr(0, state_class_name_length);
		if (state_class_name == state_class_test) {
			const char *variables[num_state_classes] = {0};
			*variables = get_variables(state_class_test);
			underscore = variable.find_first_of("_", state_class_name_length);
			id = from_string<int>(variable.substr(state_class_name_length, underscore - state_class_name_length));
			for (int i = 0; i < state_class_num_variables; ++i) {
				string variable_name = variable.substr(underscore+1);
				string variable_test = variables[i];
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
}


string Cleanup_State::variable_name (const int& variable_index) const
{	
	int variable = variable_index;
	int test_index = 0;
	for (int j = 0; j < num_state_classes; ++j) {
		string state_class_test = state_classes[j];
		int state_class_num_variables = state_classes_num_variables[j];
		const char *variables[num_state_classes] = {0};
		*variables = get_variables(state_class_test);
		int num_objects = get_num_objects(state_class_test);
		for (int i = 0; i < num_objects; ++i) {
			test_index += state_class_num_variables;
			if (variable < test_index) {
				int id = i;
				test_index -= state_class_num_variables;
				int target = variable - test_index;
				return state_class_test + to_string(id) + "_" + variables[target];
			}
		}
	}
	throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable);
}

int Cleanup_State::get_num_objects(string state_class_test) const {
	int num;
	if        (state_class_test == "agent") {
		num = 1;
	} else if (state_class_test == "block") {
		num = num_blocks;
	} else if (state_class_test == "door") {
		num = num_doors;
	} else if (state_class_test == "room") {
		num = num_rooms;
	} else {
		throw HierException(__FILE__, __LINE__, "Unknown state_class_test: " + state_class_test);
	}
	return num;
}

const char *Cleanup_State::get_variables(string state_class_test) const {
	const char *variables[num_state_classes] = {0};
	if        (state_class_test == "agent") {
		*variables = *agent_variables;
	} else if (state_class_test == "block") {
		*variables = *block_variables;
	} else if (state_class_test == "door") {
		*variables = *door_variables;
	} else if (state_class_test == "room") {
		*variables = *room_variables;
	} else {
		throw HierException(__FILE__, __LINE__, "Unknown state_class_test: " + state_class_test);
	}
	return *variables;
}


int Cleanup_State::variable_size (const int& variable_index) const
{	
	string variable = variable_name(variable_index);
	if (variable.find("x") != string::npos) {
		return max_x;
	} else if (variable.find("y") != string::npos) {
		return max_y;
	} else if (variable.find("left") != string::npos) {
		return max_x;
	} else if (variable.find("right") != string::npos) {
		return max_x;
	} else if (variable.find("bottom") != string::npos) {
		return max_y;
	} else if (variable.find("top") != string::npos) {
		return max_y;
	} else if (variable.find("shape") != string::npos) {
		return num_shapes;
	} else if (variable.find("color") != string::npos) {
		return num_colors;
	} else if (variable.find("direction") != string::npos) {
		return num_directions;
	} else if (variable.find("locked") != string::npos) {
		return max_locked_boolean_value;
	} else {
		throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}


int Cleanup_State::variable (const int& variable_index) const
{	
	
	throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable_index);
}


int& Cleanup_State::variable (const int& variable_index)
{	
	throw HierException(__FILE__, __LINE__, "int& version of variable(vindex) not implemented " + variable_index);
}


map<int,int> Cleanup_State::variables_mapper () const
{	map<int,int> var_map;
	for (int i = 0; i < num_variables; ++i)
		var_map[i] = variable(i);
	return var_map;
}


pair<bool,int> Cleanup_State::parse (string expression) const
{	
	expression = replace(expression, " ", "");
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
				throw HierException(__FILE__, __LINE__, "Cannot parse (uncomment the line above?) " + expression);
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
	out << agent.x;
	out << ",";
	out << agent.y;
	out << ",";
	out << agent.left;
	out << ",";
	out << agent.right;
	out << ",";
	out << agent.bottom;
	out << ",";
	out << agent.top;
	out << ",";
	out << agent.shape;
	out << ",";
	out << agent.color;
	out << ",";
	out << agent.direction;
	out << ")";
	
	for (const auto& block : blocks)
	{
		out << " (";
		out << block.x;
		out << ",";
		out << block.y;
		out << ",";
		out << block.left;
		out << ",";
		out << block.right;
		out << ",";
		out << block.bottom;
		out << ",";
		out << block.top;
		out << ",";
		out << block.shape;
		out << ",";
		out << block.color;
		out << ")";
	}

	for (const auto& door : doors)
	{
		out << " (";
		out << door.x;
		out << ",";
		out << door.y;
		out << ",";
		out << door.left;
		out << ",";
		out << door.right;
		out << ",";
		out << door.bottom;
		out << ",";
		out << door.top;
		out << ",";
		out << door.shape;
		out << ",";
		out << door.color;
		out << ",";
		out << door.locked;
		out << ")";
	}

	for (const auto& room : rooms)
	{
		out << " (";
		out << room.left;
		out << ",";
		out << room.right;
		out << ",";
		out << room.bottom;
		out << ",";
		out << room.top;
		out << ",";
		out << room.shape;
		out << ",";
		out << room.color;
		out << ")";
	}

	return out.str();
}

//*************************************************************************************************

Cleanup::Cleanup (const string& name, const double& success_probability) : MDP(name,
	new Cleanup_State()), reward_default(0.0), reward_goal(1.0), reward_pull(0.0), reward_noop(0.0),
	Pr_successful_execution(success_probability)
{	
	map_creator();
	initialize();
}


void Cleanup::map_creator (const int& mode)
{	
	three_rooms();
}

void Cleanup::three_rooms () {

	// make rooms

	Cleanup_State::Room big_room;
	big_room.left = 0;
	big_room.right = max_x-1;
	big_room.bottom = max_y/2;
	big_room.top = max_y-1;
	big_room.color = room_colors[rand_int(num_room_colors)];
	big_room.shape = room;
	
	Cleanup_State::Room room1;
	room1.left = big_room.left;
	room1.right = big_room.right/2;
	room1.bottom = min_y;
	room1.top = big_room.bottom;
	room1.color = room_colors[rand_int(num_room_colors)];
	room1.shape = room;
	
	Cleanup_State::Room room2;
	room2.left = big_room.right/2;
	room2.right = big_room.right;
	room2.bottom = min_y;
	room2.top = big_room.bottom;
	room2.color = room_colors[rand_int(num_room_colors)];
	room2.shape = room;
	



	// make doors

	// make agent

	// make blocks

}


void Cleanup::initialize (const bool& target)
{	

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
				++state().agent.y;
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case south:
			if (rand_real() < Pr_successful_execution)
			{	
				--state().agent.y;
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case east:
			if (rand_real() < Pr_successful_execution)
			{	
				++state().agent.x;
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case west:
			if (rand_real() < Pr_successful_execution)
			{
				--state().agent.x;
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
			