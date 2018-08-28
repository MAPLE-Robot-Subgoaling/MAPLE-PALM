
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
	} else {
		throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable);
	}
}

int Cleanup_State::debug_num_states () const 
{
	cout << "Warning: hard-coded of states\n";
	return 100000;//2000;
}

int Cleanup_State::variable_size_for_state (const int& variable_index) const
{	
	string variable = variable_name(variable_index);
	if (variable.find("x") != string::npos) {
		// possible x coords for agent, block, or door
		// 1, 2, 3, 4, 5
		return 5;
	} else if (variable.find("y") != string::npos) {
		// possible y coords for agent, block, or door
		// 1, 2, 3, 4, 5
		return 5;
	} else if (variable.find("left") != string::npos) {
		// possible values for room
		// 0, 3
		return 2;
	} else if (variable.find("right") != string::npos) {
		// possible values for room
		// 3, 6
		return 2;
	} else if (variable.find("bottom") != string::npos) {
		// possible values for room
		// 0, 3
		return 2;
	} else if (variable.find("top") != string::npos) {
		// possible values for room
		// 0, 3
		return 3, 6;
	} else if (variable.find("shape") != string::npos) {
		// max num of shapes == num of blocks in this state
		return num_blocks;
	} else if (variable.find("color") != string::npos) {
		// max num of colors == num of rooms + num blocks in this state
		return num_rooms + num_blocks;
	} else if (variable.find("direction") != string::npos) {
		return num_directions;
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
						return agent.position.x;
					} else if (variable_name == "y") {
						return agent.position.y;
					} else if (variable_name == "direction") {
						return agent.direction;
					} else {
						throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
					}
				} else if (state_class_test == "block") {
					if (variable_name == "x") {
						return blocks[id].position.x;
					} else if (variable_name == "y") {
						return blocks[id].position.y;
					} else if (variable_name == "shape") {
						return blocks[id].shape;
					} else if (variable_name == "color") {
						return blocks[id].color;
					} else {
						throw HierException(__FILE__, __LINE__, "Unknown variable index: " + variable_index);
					}
				} else if (state_class_test == "door") {
					if (variable_name == "x") {
						return doors[id].position.x;
					} else if (variable_name == "y") {
						return doors[id].position.y;
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
			//else if (token_str == "cyan")
			//	value = cyan;
			//else if (token_str == "orange")
			//	value = orange;
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
	in >> agent.position;
	int direction;
	in >> direction;
	agent.direction = static_cast<Direction>(direction);
	
	for (int i = 0; i < num_blocks; ++i) {
		Cleanup_State::Block block = blocks[i];
		in >> block.position;
		int shape;
		in >> shape;
		block.shape = static_cast<Shape>(shape);
		int color;
		in >> color;
		block.color = static_cast<Color>(color);
		blocks[i] = block;
	}

	for (int i = 0; i < num_doors; ++i) {
		Cleanup_State::Door door = doors[i];
		in >> door.position;
		doors[i] = door;
	}

	for (int i = 0; i < num_rooms; ++i) {
		Cleanup_State::Room room = rooms[i];
		int left, right, bottom, top;
		in >> left;
		in >> right;
		in >> bottom;
		in >> top;
		int color;
		in >> color;
		room.color = static_cast<Color>(color);
		rooms[i] = room;
	}

}
//{
//	throw HierException(__FILE__, __LINE__, "ERROR: \nBad state read.\n");
//}


string Cleanup_State::print () const
{	
	ostringstream out;
	
	// WARNING: the output MUST follow the exact same format as in taxi.cc 
	// since this is processed by a regex and used in model-building

	out << "(";
	out << agent.position.x;
	out << ",";
	out << agent.position.y;
	out << ") ";
	out << agent.direction;
	out << " ";
	
	for (const auto& block : blocks)
	{
		out << "(";
		out << block.position.x;
		out << ",";
		out << block.position.y;
		out << ") ";
		out << block.shape;
		out << " ";
		out << block.color;
		out << " ";
	}

	for (const auto& door : doors)
	{
		out << "(";
		out << door.position.x;
		out << ",";
		out << door.position.y;
		out << ") ";
	}

	bool first = true;
	for (const auto& room : rooms)
	{
		if (!first) {
			out << " ";
		}
		first = false;
		out << room.left;
		out << " ";
		out << room.right;
		out << " ";
		out << room.bottom;
		out << " ";
		out << room.top;
		out << " ";
		out << room.color;
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
	while (room1.color == big_room.color)
	{ room1.color = room_colors[rand_int(num_room_colors)]; }
	
	Cleanup_State::Room room2 = {};
	room2.left = big_room.right/2;
	room2.right = big_room.right;
	room2.bottom = min_y;
	room2.top = big_room.bottom;
	room2.color = room_colors[rand_int(num_room_colors)];
	while (room2.color == big_room.color || room2.color == room1.color)
	{ room2.color = room_colors[rand_int(num_room_colors)]; }
	
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

	door0.position.x = dx0 - rand_int(2);
	door0.position.y = dy0;

	door1.position.x = dx1 - rand_int(2);
	door1.position.y = dy1;

	door2.position.x = dx2;
	door2.position.y = dy2 + rand_int(2);

	state().doors[0] = door0;
	state().doors[1] = door1;
	state().doors[2] = door2;

	// make agent
	int ax = 0;
	int ay = 0;
	while (wall_at(ax, ay)) {
		ax = rand_int(max_x);
		ay = rand_int(max_y);
	}
	Cleanup_State::Agent agent = {};
	agent.position.x = ax;
	agent.position.y = ay;
	agent.direction = directions[rand_int(num_directions)];
	
	state().agent = agent;

	// make blocks
	Color possible_block_colors[] = {big_room.color, room1.color, room2.color};
	int num_possible_block_colors = 3;
	for (int i = 0; i < state().num_blocks; ++i) {
		Cleanup_State::Block block = {};
		if (i == 0) {
			// special, the first block MUST be a bag or backpack
			// because it will be a goal block
			block.shape = goal_block_shapes[rand_int(num_goal_block_shapes)];
		} else {
			//block.shape = nongoal_block_shapes[rand_int(num_nongoal_block_shapes)];
			block.shape = block_shapes[rand_int(num_block_shapes)];
		}
		block.color = possible_block_colors[rand_int(num_possible_block_colors)];
		state().blocks[i] = block;
		//do {
		//	block.color = block_colors[rand_int(num_block_colors)];
		//} while(
		//	!(block.color == big_room.color || block.color == room1.color || block.color == room2.color)
		//);
		int bx = -99;
		int by = -99;
		while (wall_at(bx, by) || block_at(bx, by) || agent_at(bx, by) || terminated()
			|| ((block.shape == bag || block.shape == backpack) && is_inside_room_of_same_color(block))) { //|| is_inside(block, 1)) {
			bx = rand_int(max_x);
			by = rand_int(max_y);
			block.position.x = bx;
			block.position.y = by;
		}
		state().blocks[i] = block;
	}

	cout << "three rooms done.";
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

	if (state().blocks[0].position.x == 0 || state().blocks[0].position.y == 0) {
		cout << "ERROR blocks";
	}
	if (state().agent.position.x == 0 || state().agent.position.y == 0) {
		cout << "ERROR agent";
	}


}

void Cleanup::do_move (int dx, int dy) {
	int nx = state().agent.position.x + dx;
	int ny = state().agent.position.y + dy;
	int nbx = nx;
	int nby = ny;

	bool agent_can_move = false;
	bool block_can_move = false;

	bool block_in_the_way = false;
	int pushed_block_index = -99;
	Cleanup_State::Block pushed_block;
	for (int i = 0; i < state().num_blocks; ++i) {
		Cleanup_State::Block block = state().blocks[i];
		if (block.position.x == nx && block.position.y == ny) {
			block_in_the_way = true;
			pushed_block = block;
			pushed_block_index = i;
			break;
		}
	}
	if (block_in_the_way) {
		int bx = pushed_block.position.x;
		int by = pushed_block.position.y;
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
			pushed_block.position.x = nbx;
			pushed_block.position.y = nby;
			state().blocks[pushed_block_index] = pushed_block;
		}
		state().agent.position.x = nx;
		state().agent.position.y = ny;
	}
	Direction new_direction;
	if (dy > 0) { new_direction = Direction::north; }
	else if (dy < 0) { new_direction = Direction::south; }
	else if (dx > 0) { new_direction = Direction::east; }
	else if (dx < 0) { new_direction = Direction::west; }
	state().agent.direction = new_direction;
}

void Cleanup::do_pull () {
	int ax = state().agent.position.x;
	int ay = state().agent.position.y;
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
		if (block.position.x == nx && block.position.y == ny) {
			block_in_the_way = true;
			pulled_block = block;
			pulled_block_index= i;
			break;
		}
	}
	if (block_in_the_way) {
		int bx = pulled_block.position.x;
		int by = pulled_block.position.y;
		int nbx = ax;
		int nby = ay;
		pulled_block.position.x = nbx;
		pulled_block.position.y = nby;
		state().blocks[pulled_block_index] = pulled_block;
		
		state().agent.position.x = nx;
		state().agent.position.y = ny;
		state().agent.direction = new_direction;
	}
}

bool Cleanup::terminated () const
{	
	return is_all_inside_room_of_same_color(bag) && is_all_inside_room_of_same_color(backpack); //is_inside_room_of_same_color(0);// && block_at(3, 3);//is_inside(0, 1); // || is_inside(0, 2); // || is_inside(0,0)
}

bool Cleanup::is_all_inside_room_of_same_color (Shape shape) const
{
	for (int i = 0; i < state().num_blocks; ++i) {
		if (shape == state().blocks[i].shape) {
			bool inside = is_inside_room_of_same_color(i);
			if (!inside) return false;
		}
	}
	return true;
}

bool Cleanup::is_open(int x, int y) {
	return !(wall_at(x, y) || block_at(x, y));
}

bool Cleanup::agent_at(int x, int y) const {
	if (state().agent.position.x == x && state().agent.position.y == y) {
		return true;
	}
	return false;
}

bool Cleanup::door_at(int x, int y) {
	for (int i = 0; i < state().num_doors; ++i) {
		Cleanup_State::Door door = state().doors[i];
		if (door.position.x == x && door.position.y == y) {
			return true;
		}
	}
	return false;
}

bool Cleanup::block_at(int x, int y) const {
	for (int i = 0; i < state().num_blocks; ++i) {
		Cleanup_State::Block block = state().blocks[i];
		if (block.position.x == x && block.position.y == y) {
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

bool Cleanup::is_inside_room_of_same_color(int block_index) const {
	Cleanup_State::Block block = state().blocks[block_index]; 
	return is_inside_room_of_same_color(block);
}

bool Cleanup::is_inside_room_of_same_color(Cleanup_State::Block block) const {
	for (int room_index = 0; room_index < state().num_rooms; ++room_index) {
		if (is_inside(block, room_index)) {
			Cleanup_State::Room room = state().rooms[room_index];
			Color room_color = room.color;
			Color block_color = block.color;
			if (room_color == block_color) return true;
		}
	}
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
	if (block.position.y > room.bottom && block.position.y < room.top && block.position.x > room.left && block.position.x < room.right) {
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
			