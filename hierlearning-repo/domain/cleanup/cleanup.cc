
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
			const char **variables;
			variables = get_variables(state_class_test);
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
		const char **variables;
		variables = get_variables(state_class_test);
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

const char **Cleanup_State::get_variables(string state_class_test) const {
	const char **variables;
	if        (state_class_test == "agent") {
		variables = agent_variables;
	} else if (state_class_test == "block") {
		variables = block_variables;
	} else if (state_class_test == "door") {
		variables = door_variables;
	} else if (state_class_test == "room") {
		variables = room_variables;
	} else {
		throw HierException(__FILE__, __LINE__, "Unknown state_class_test: " + state_class_test);
	}
	return variables;
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
	int variable = variable_index;
	int test_index = 0;
	for (int j = 0; j < num_state_classes; ++j) {
		string state_class_test = state_classes[j];
		int state_class_num_variables = state_classes_num_variables[j];
		const char **variables;
		variables = get_variables(state_class_test);
		int num_objects = get_num_objects(state_class_test);
		for (int i = 0; i < num_objects; ++i) {
			test_index += state_class_num_variables;
			if (variable < test_index) {
				int id = i;
				test_index -= state_class_num_variables;
				int target = variable - test_index;
				string variable_name = variables[target];
				if (state_class_test == "agent") {
					if (variable_name == "x") {
						return agent.x;
					} else if (variable_name == "y") {
						return agent.y;
					} else if (variable_name == "direction") {
						return agent.direction;
					} else {
						throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
					}
				} else if (state_class_test == "block") {
					if (variable_name == "x") {
						return blocks[id].x;
					} else if (variable_name == "y") {
						return blocks[id].y;
					} else if (variable_name == "shape") {
						return blocks[id].shape;
					} else if (variable_name == "color") {
						return blocks[id].color;
					} else {
						throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
					}
				} else if (state_class_test == "door") {
					if (variable_name == "x") {
						return doors[id].x;
					} else if (variable_name == "y") {
						return doors[id].y;
					} else {
						throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
					}
				} else if (state_class_test == "room") {
					if (variable_name == "left") {
						return rooms[id].left;
					} else if (variable_name == "right") {
						return rooms[id].right;
					} else if (variable_name == "bottom") {
						return rooms[id].bottom;
					} else if (variable_name == "top") {
						return rooms[id].top;
					} else if (variable_name == "color") {
						return rooms[id].color;
					} else {
						throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
					}
				} else {
					throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
				}
			}
		}
	}
}


int& Cleanup_State::variable (const int& variable_index)
{	
	throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
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
			string token_str = token.str();
			if (token_str == "red")
				value = red;
			else if (token_str == "green")
				value = green;
			else if (token_str == "blue")
				value = blue;
			else if (token_str == "yellow")
				value = yellow;
			else if (token_str == "magenta")
				value = magenta;
			else if (token_str == "cyan")
				value = cyan;
			else if (token_str == "orange")
				value = orange;
			else if (token_str == "chair")
				value = chair;
			else if (token_str == "bag")
				value = bag;
			else if (token_str == "backpack")
				value = backpack;
			else if (token_str == "basket")
				value = basket;
			else if (token_str == "north")
				value = north;
			else if (token_str == "south")
				value = south;
			else if (token_str == "east")
				value = east;
			else if (token_str == "west")
				value = west;
			else if (token_str == "map_size.x")
				value = map_size.x;
			else if (token_str == "map_size.y")
				value = map_size.y;
			else
				return make_pair(false, 0);
				//throw HierException(__FILE__, __LINE__, "Cannot parse (uncomment the line above?) " + expression);
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
	
	// WARNING: the output MUST follow the exact same format as in taxi.cc 
	// since this is processed by a regex and used in model-building

	out << "(";
	out << agent.x;
	out << ",";
	out << agent.y;
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
	// used for things that persist across reseting the episode
	// not needed for cleanup
}

void Cleanup::three_rooms () {

	// make rooms

	Cleanup_State::Room big_room = {};
	big_room.left = 0;
	big_room.right = max_x-1;
	big_room.bottom = max_y/2;
	big_room.top = max_y-1;
	big_room.color = room_colors[rand_int(num_room_colors)];
	
	Cleanup_State::Room room1 = {};
	room1.left = big_room.left;
	room1.right = big_room.right/2;
	room1.bottom = min_y;
	room1.top = big_room.bottom;
	room1.color = room_colors[rand_int(num_room_colors)];
	
	Cleanup_State::Room room2 = {};
	room2.left = big_room.right/2;
	room2.right = big_room.right;
	room2.bottom = min_y;
	room2.top = big_room.bottom;
	room2.color = room_colors[rand_int(num_room_colors)];
	
	state().rooms[0] = big_room;
	state().rooms[1] = room1;
	state().rooms[2] = room2;
	
	// make doors

	Cleanup_State::Door door0 = {};
	Cleanup_State::Door door1 = {};
	Cleanup_State::Door door2 = {};
	int dx0 = big_room.right/3;
	int dx1 = 2*big_room.right/3 + 1;
	int dx2 = big_room.right/2;
	int dy0 = big_room.bottom;
	int dy1 = big_room.bottom;
	int dy2 = big_room.bottom/2;

	door0.x = dx0;
	door0.y = dy0;

	door1.x = dx1;
	door1.y = dy1;

	door2.x = dx2;
	door2.y = dy2;

	state().doors[0] = door0;
	state().doors[1] = door1;
	state().doors[2] = door2;

	// make agent
	
	int ax = dx2;
	int ay = dy2;
	Cleanup_State::Agent agent = {};
	agent.x = ax;
	agent.y = ay;
	agent.direction = directions[rand_int(num_directions)];
	
	state().agent = agent;

	// make blocks

	for (int i = 0; i < state().num_blocks; ++i) {
		Cleanup_State::Block block = {};
		int bx = -99;
		int by = -99;
		while (wall_at(bx, by) || block_at(bx, by) || agent_at(bx, by) || is_inside(block, 0) || is_inside(block, 1) || is_inside(block, 2)) {
			bx = rand_int(max_x);
			by = rand_int(max_y);
			block.x = bx;
			block.y = by;
		}
		block.color = block_colors[rand_int(num_block_colors)];
		block.shape = block_shapes[rand_int(num_block_shapes)];
		state().blocks[i] = block;
	}

	cout << "three rooms done\n";
}


void Cleanup::initialize (const bool& target)
{	

	_reward = 0.0;
	_duration = 0.0;

	three_rooms();
}


void Cleanup::process (const vector<int>& action)
{	
	_reward = reward_default;
	_duration = 1.0;

	switch (action[0])
	{	case north:
			if (rand_real() < Pr_successful_execution)
			{	
				do_move(0,1);
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case south:
			if (rand_real() < Pr_successful_execution)
			{	
				do_move(0,-1);
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case east:
			if (rand_real() < Pr_successful_execution)
			{	
				do_move(1,0);
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case west:
			if (rand_real() < Pr_successful_execution)
			{
				do_move(-1,0);
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			break;

		case pull:
			if (rand_real() < Pr_successful_execution)
			{ 
				do_pull();
			} else {
				throw new HierException(__FILE__, __LINE__, "Bad action roll?");
			}
			_reward += reward_pull;
			break;

		default:
			throw HierException(__FILE__, __LINE__, "Unknown action.");
	}

	if (terminated()) {
		_reward += reward_goal;
	} else {
		_reward += reward_default;
	}


}

void Cleanup::do_move (int dx, int dy) {
	int nx = state().agent.x + dx;
	int ny = state().agent.y + dy;
	int nbx = nx;
	int nby = ny;

	bool agent_can_move = false;
	bool block_can_move = false;

	bool block_in_the_way = false;
	int pushed_block_index = -99;
	Cleanup_State::Block pushed_block;
	for (int i = 0; i < state().num_blocks; ++i) {
		Cleanup_State::Block block = state().blocks[i];
		if (block.x == nx && block.y == ny) {
			block_in_the_way = true;
			pushed_block = block;
			pushed_block_index = i;
			break;
		}
	}
	if (block_in_the_way) {
		int bx = pushed_block.x;
		int by = pushed_block.y;
		nbx = bx + dx;
		nby = by + dy;
		if (is_open(nbx, nby)) {
			block_can_move = true;
			agent_can_move = true;
		} else {
			block_can_move = false;
			agent_can_move = false;
		}
	} else {
		if (wall_at(nx, ny)) {
			agent_can_move = false;
		} else {
			agent_can_move = true;
		}
	}

	if (agent_can_move) {
		if (block_can_move) {
			pushed_block.x = nbx;
			pushed_block.y = nby;
			state().blocks[pushed_block_index] = pushed_block;
		}
		Direction new_direction;
		if (dy > 1) { new_direction = Direction::north; }
		else if (dy < 1) { new_direction = Direction::south; }
		else if (dx > 1) { new_direction = Direction::east; }
		else if (dx < 1) { new_direction = Direction::west; }
		state().agent.x = nx;
		state().agent.y = ny;
		state().agent.direction = new_direction;
	}
}

void Cleanup::do_pull () {
	int ax = state().agent.x;
	int ay = state().agent.y;
	int dx = 0;
	int dy = 0;
	Direction direction = state().agent.direction;
	Direction new_direction;
	if (direction == north) {
		dy += 1;
		new_direction = Direction::south;
	} else if (direction == south) {
		dy -= 1;
		new_direction = Direction::north;
	} else if (direction == east) {
		dx += 1;
		new_direction = Direction::west;
	} else if (direction == west) {
		dx -= 1;
		new_direction = Direction::east;
	} else {
		throw HierException(__FILE__, __LINE__, "Unknown direction in do_pull");
	}
	int nx = ax + dx;
	int ny = ay + dy;
	int pulled_block_index = -99;
	bool block_in_the_way = false;
	Cleanup_State::Block pulled_block;
	for (int i = 0; i < state().num_blocks; ++i) {
		Cleanup_State::Block block = state().blocks[i];
		if (block.x == nx && block.y == ny) {
			block_in_the_way = true;
			pulled_block = block;
			pulled_block_index= i;
			break;
		}
	}
	if (block_in_the_way) {
		int bx = pulled_block.x;
		int by = pulled_block.y;
		int nbx = ax;
		int nby = ay;
		pulled_block.x = nbx;
		pulled_block.y = nby;
		state().blocks[pulled_block_index] = pulled_block;
		
		state().agent.x = nx;
		state().agent.y = ny;
		state().agent.direction = new_direction;
	}
}

bool Cleanup::terminated () const
{	
	return is_inside(0, 1) || is_inside(0,0) || is_inside(0, 2);
}

bool Cleanup::is_open(int x, int y) {
	return !(wall_at(x, y) || block_at(x, y));
}

bool Cleanup::agent_at(int x, int y) {
	if (state().agent.x == x && state().agent.y == y) {
		return true;
	}
	return false;
}

bool Cleanup::door_at(int x, int y) {
	for (int i = 0; i < state().num_doors; ++i) {
		Cleanup_State::Door door = state().doors[i];
		if (door.x == x && door.y == y) {
			return true;
		}
	}
	return false;
}

bool Cleanup::block_at(int x, int y) {
	for (int i = 0; i < state().num_blocks; ++i) {
		Cleanup_State::Block block = state().blocks[i];
		if (block.x == x && block.y == y) {
			return true;
		}
	}
	return false;
}

bool Cleanup::wall_at(int x, int y) {
	// trivially, there is a wall if out of bounds
	if (x < min_x || x >= max_x || y < min_y || y >= max_y) {
		return true;
	}
	for (int i = 0; i < state().num_rooms; ++i) {
		Cleanup_State::Room room = state().rooms[i];
		int left = room.left;
		int right = room.right;
		int bottom = room.bottom;
		int top = room.top;
		// if inside a wall
		if (((x == left || x == right) && y >= bottom && y <= top) || ((y == bottom || y == top) && x >= left && x <= right)) {
			// check if door is there
			bool door_there = door_at(x, y);
			return !door_there;
		}
	}
	// not inside a wall
	return false;
}

bool Cleanup::is_inside(int block_index, int room_index) const {
	Cleanup_State::Block block = state().blocks[block_index]; 
	Cleanup_State::Room room = state().rooms[room_index]; 
	return is_inside(block, room);
}

bool Cleanup::is_inside(Cleanup_State::Block block, int room_index) const {
	Cleanup_State::Room room = state().rooms[room_index]; 
	return is_inside(block, room);
}


bool Cleanup::is_inside(Cleanup_State::Block block, Cleanup_State::Room room) const {
	if (block.y > room.bottom && block.y < room.top && block.x > room.left && block.x < room.right) {
		return true;
	}
	return false;
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
			