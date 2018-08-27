
/*************************************************

	WARGUS DOMAIN
		Neville Mehta

**************************************************/


#include <sstream>
#include "wargus.h"



Wargus_State::Wargus_State (const unsigned& num_agents) : num_global_variables(2), effective_range(4), goldmine_width(3), townhall_width(4), num_resources(2)
{	peasants.resize(num_agents);
	agent_region_gold.resize(num_agents, NA);
	agent_region_wood.resize(num_agents, NA);
	agent_region_townhall.resize(num_agents, NA);
}


vector<int> Wargus_State::variables () const
{	vector<string> state_variable_tokens = tokenize("agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", ", ");
	vector<int> state_variables;
	for (const auto& state_variable_token : state_variable_tokens)
		state_variables.push_back(variable_index(state_variable_token));
	return state_variables;
}


// Variable name to integer index
int Wargus_State::variable_index (const string& variable_name) const
{	// Agent-dependent variables are positively indexed; global variables are negatively indexed
	if (variable_name == "agent_x")
		return 0;
	if (variable_name == "agent_y")
		return 1;
	if (variable_name == "agent_resource")
		return 2;
	if (variable_name == "agent_region_gold")
		return 3;
	if (variable_name == "agent_region_wood")
		return 4;
	if (variable_name == "agent_region_townhall")
		return 5;

	// Agent variables are named "agent#_<variable name>"
	if (variable_name.substr(0, 5) == "agent")
	{	unsigned agent = from_string<int>(variable_name.substr(5, variable_name.find_first_of("_") - 5));
		if (agent >= num_agents())
			throw HierException(__FILE__, __LINE__, "Not a valid agent id.");

		if (variable_name.substr(variable_name.find_first_of("_")) == "_x")
			return num_agent_variables() * agent;
		else if (variable_name.substr(variable_name.find_first_of("_")) == "_y")
			return num_agent_variables() * agent + 1;
		else if (variable_name.substr(variable_name.find_first_of("_")) == "_resource")
			return num_agent_variables() * agent + 2;
		else if (variable_name.substr(variable_name.find_first_of("_")) == "_region_gold")
			return num_agent_variables() * agent + 3;
		else if (variable_name.substr(variable_name.find_first_of("_")) == "_region_wood")
			return num_agent_variables() * agent + 4;
		else if (variable_name.substr(variable_name.find_first_of("_")) == "_region_townhall")
			return num_agent_variables() * agent + 5;
	}
	else if (variable_name == "requisite_gold")
		return -1;
	else if (variable_name == "requisite_wood")
		return -2;

	throw HierException(__FILE__, __LINE__, "Unknown variable \"" + variable_name + "\".");
}


string Wargus_State::variable_name (const int& variable_index) const
{	if (variable_index >= 0)
	{	switch (variable_index % num_agent_variables())
		{	case 0:
				return "agent_x";
			case 1:
				return "agent_y";
			case 2:
				return "agent_resource";
			case 3:
				return "agent_region_gold";
			case 4:
				return "agent_region_wood";
			case 5:
				return "agent_region_townhall";
			default:
				throw HierException(__FILE__, __LINE__, "Unknown variable #" + to_string(variable_index) + ".");
		}
	}
	else
		switch (abs(variable_index))
		{	case 1:
				return "requisite_gold";
			case 2:
				return "requisite_wood";
			default:
				throw HierException(__FILE__, __LINE__, "Unknown variable #" + to_string(variable_index) + ".");
		}
}


// Arity of a variable
int Wargus_State::variable_size (const int& variable_index) const
{	if (variable_index >= 0)
	{	switch (variable_index % num_agent_variables())
		{	case 0:   // x
				return map_size().x;
			case 1:   // y
				return map_size().y;
			case 2:   // resource
				return num_resources + 1;
			case 3:   // gold_in_region
			case 4:   // wood_in_region
			case 5:   // townhall_in_region
				return 2;
			default:
				throw HierException(__FILE__, __LINE__, "Unknown variable #" + to_string(variable_index) + ".");
		}
	}
	else
		return 2;   // Requisite gold & wood
}

int Wargus_State::variable_size_for_state (const int& variable_index) const
{
	return variable_size(variable_index);
}

int Wargus_State::variable (const int& variable_index) const
{	if (variable_index >= 0)
	{	unsigned agent = variable_index / num_agent_variables();

		switch (variable_index % num_agent_variables())
		{	case 0:
				return peasants[agent].coord.x;
			case 1:
				return peasants[agent].coord.y;
			case 2:
				return peasants[agent].resource;
			case 3:
				return agent_region_gold[agent];
			case 4:
				return agent_region_wood[agent];
			case 5:
				return agent_region_townhall[agent];
			default:
				throw HierException(__FILE__, __LINE__, "Unknown variable #" + to_string(variable_index) + ".");
		}
	}
	else if (variable_index == -1)
		return requisite_resources.gold;
	else if (variable_index == -2)
		return requisite_resources.wood;
	else
		throw HierException(__FILE__, __LINE__, "Unknown variable #" + to_string(variable_index) + ".");
}


int& Wargus_State::variable (const int& variable_index)
{	if (variable_index >= 0)
	{	unsigned agent = variable_index / num_agent_variables();

		switch (variable_index % num_agent_variables())
		{	case 0:
				return peasants[agent].coord.x;
			case 1:
				return peasants[agent].coord.y;
			case 2:
				return peasants[agent].resource;
			case 3:
				return agent_region_gold[agent];
			case 4:
				return agent_region_wood[agent];
			case 5:
				return agent_region_townhall[agent];
			default:
				throw HierException(__FILE__, __LINE__, "Illegal variable assignment.");
		}
	}
	else if (variable_index == -1)
		return requisite_resources.gold;
	else if (variable_index == -2)
		return requisite_resources.wood;
	else
		throw HierException(__FILE__, __LINE__, "Unknown variable #" + to_string(variable_index) + ".");
}


map<int,int> Wargus_State::variables_mapper () const
{	map<int,int> var_map;
	for (unsigned i = 0; i < num_agent_variables(); ++i)
		var_map[i] = variable(i);
	for (int i = 1; i <= (int)num_global_variables; ++i)
		var_map[-i] = variable(-i);
	return var_map;
}


bool Wargus_State::action_complete (const unsigned& agent) const
{	return peasants[agent].status == 1;
}


Coordinate Wargus_State::map_size () const
{	return Coordinate(map_layout.num_rows(), map_layout.num_cols());
}


void Wargus_State::draw_map () const
{	for (int y = 0; y < map_size().y; ++y)
	{	for (int x = 0; x < map_size().x; ++x)
			cout << map_layout(x, y);
		cout << endl;
	}
	cout << endl;
}

int Wargus_State::debug_num_states() const 
{
	throw HierException(__FILE__, __LINE__, "ERROR: debug num states is being used (needed for Cleanup)");
}

pair<bool,int> Wargus_State::parse (string expression) const
{	int total = 0;

	// Delete spaces
	while (expression.find(" ") != string::npos)
		expression.replace(expression.find(" "), 1, "");

	expression.insert(0, "+");   // Explicit sign for positive numbers at the start

	string::size_type start = expression.find_first_not_of("+-", 0);
	string::size_type end = expression.find_first_of("+-", start);
	while (start != string::npos || end != string::npos)
	{	istringstream token(expression.substr(start, end - start));
		int value;
		if (!(token >> value))   // 'token' is not an integer
		{	if (token.str() == "num_agent_variables")
				value = num_agent_variables();
			else if (token.str() == "NL")
				value = NL;
			else if (token.str() == "Gold")
				value = Gold;
			else if (token.str() == "Wood")
				value = Wood;
			else if (token.str() == "Oil")
				value = Oil;
			else
				return make_pair(false, 0);
		}
		total += (expression[start-1] == '+') ? value : -value;

		start = expression.find_first_not_of("+-", end);
		end = expression.find_first_of("+-", start);
	}

	return make_pair(true, total);
}


void Wargus_State::read (istream& in)
{	for (unsigned agent = 0; agent < num_agents(); ++agent)
	{	if (in >> peasants[agent].coord)
		{	in >> peasants[agent].resource;
			in >> agent_region_gold[agent];
			in >> agent_region_wood[agent];
			in >> agent_region_townhall[agent];
		}
		else
			throw HierException(__FILE__, __LINE__, "Bad state read.");
	}

	in >> requisite_resources.gold;
	in >> requisite_resources.wood;
}


string Wargus_State::print () const
{	ostringstream out;

	for (unsigned agent = 0; agent < num_agents(); ++agent)
	{	if (agent > 0)
			out << " ";

		out << "(";
		if (peasants[agent].coord.x != NA)
			out << peasants[agent].coord.x;
		else
			out << "*";
		out << ",";
		if (peasants[agent].coord.y != NA)
			out << peasants[agent].coord.y;
		else
			out << "*";
		out << ")";

		if (peasants[agent].resource != NA)
			out << " " << peasants[agent].resource;
		else
			out << " *";

		if (agent_region_gold[agent] != NA)
			out << " " << agent_region_gold[agent];
		else
			out << " *";

		if (agent_region_wood[agent] != NA)
			out << " " << agent_region_wood[agent];
		else
			out << " *";

		if (agent_region_townhall[agent] != NA)
			out << " " << agent_region_townhall[agent];
		else
			out << " *";
	}

	if (requisite_resources.gold != NA)
		out << " " << requisite_resources.gold;
	else
		out << " *";

	if (requisite_resources.wood != NA)
		out << " " << requisite_resources.wood;
	else
		out << " *";

	return out.str();
}

//*****************************************************************************************

Wargus::Wargus (const string& name, ClientSocket* const socket, const string& source_map_name, const unsigned& num_source_maps, const Wargus_State::Resources& source_resource_quotas, const string& target_map_name, const unsigned& num_target_maps, const Wargus_State::Resources& target_resource_quotas, const int& speed, const int& cycles)
					: MDP(name, new Wargus_State(0)), socket(socket), source_map_name(source_map_name), num_source_maps(num_source_maps), source_resource_quotas(source_resource_quotas), target_map_name(target_map_name), num_target_maps(num_target_maps), target_resource_quotas(target_resource_quotas), target_map_initialized(false), reward_default(-1), reward_deposit(-1)
{	// Check that all source maps exist
	for (unsigned m = 0; m < num_source_maps; ++m)
	{	if (!entity_exists("../other/stratagus/data/" + source_map_name + to_string(m) + ".pud.gz"))
			throw HierException(__FILE__, __LINE__, "Map ../other/stratagus/" + source_map_name + to_string(m) + ".pud.gz does not exist.");
	}

	// Check that all target maps exist
	for (unsigned m = 0; m < num_target_maps; ++m)
	{	if (!entity_exists("../other/stratagus/data/" + target_map_name + to_string(m) + ".pud.gz"))
			throw HierException(__FILE__, __LINE__, "Map ../other/stratagus/" + target_map_name + to_string(m) + ".pud.gz does not exist.");
	}

	set_speed(speed, cycles);
}


valarray<double> Wargus::reward () const
{	valarray<double> current_reward(0.0, _reward.size());
	for (unsigned agent = 0; agent < _reward.size(); ++agent)
		if (state().action_complete(agent))
			current_reward[agent] = _reward[agent];
	return current_reward;
}


void Wargus::initialize (const bool& target)
{	if (target)
	{	if (target_map_initialized)
			reset();
		else
		{	load_map(target_map_name + to_string(rand_int(num_target_maps)) + ".pud.gz");
			state().resource_quotas = target_resource_quotas;
			target_map_initialized = true;
		}
	}
	else
	{	load_map(source_map_name + to_string(rand_int(num_source_maps)) + ".pud.gz");
		state().resource_quotas = source_resource_quotas;
	}

	socket->send("l g\n");
	read_socket(true);

	string::size_type index = 0;
	player_id = tokenizer("player-id", index);

	Coordinate map_size;
	map_size.x = tokenizer("width", index);
	map_size.y = tokenizer("length", index);
	state().map_layout.resize(map_size.x, map_size.y);

	state_update();
	_duration.resize(state().num_agents());
	_reward.resize(state().num_agents());
}


void Wargus::read_socket (const bool parentheses_matching, const unsigned& num_lines)
{	if (parentheses_matching)
	{	unsigned open_parens = 0, close_parens = 0;
		string buffer;
		response.clear();
		do
		{	if (!socket->receive(buffer))
				throw HierException(__FILE__, __LINE__, "Connection closed by Stratagus server.");
			for (const auto& c : buffer)
			{	if (c == '(')
					++open_parens;
				else if (c == ')')
					++close_parens;
			}
			response += buffer;
		} while (open_parens != close_parens || response[response.size()-1] != '\n');
	}
	else
	{	unsigned received_lines = 0;
		string buffer;
		response.clear();
		do
		{	if (!socket->receive(buffer))
				throw HierException(__FILE__, __LINE__, "Connection closed by Stratagus server.");
			for (const auto& c : buffer)
				if (c == '\n')
					++received_lines;
			response += buffer;
		} while (received_lines < num_lines);
	}
}


int Wargus::tokenizer (const string& search_str, string::size_type& index) const
{	string::size_type start = response.find_first_of("0123456789", response.find(search_str, index));
	index = response.find_first_not_of("0123456789", start);

	return from_string<int>(response.substr(start, index - start));
}


void Wargus::load_map (const string& map_filename)
{	socket->send("m " + map_filename + "\n");
	read_socket(false);
}


void Wargus::read_map ()
{	socket->send("l m\n");
	read_socket(true);

	string::size_type start = response.find_first_not_of("()\n");
	for (int y = 0; y < state().map_size().y; ++y)
	{	for (int x = 0; x < state().map_size().x; ++x)
			state().map_layout(x, y) = response[start + x];
		start = response.find_first_not_of("()\n", start + state().map_size().x);
	}
}


void Wargus::state_update ()
{	read_map();

	vector<Wargus_State::Unit> peasant_previous_state = state().peasants;
	state().peasants.clear();
	state().townhalls.clear();
	state().goldmines.clear();

	socket->send("l s\n");
	read_socket(true);

//	cout << response << endl;   // Causes drastic (stochastic) slowdown in Windows when attempting to run at full speed

	string::size_type index = 0;
	while (response.find(" . ", index) != string::npos)
	{	index = response.find_last_of('(', response.find(" . ", index));

		Wargus_State::Unit unit;
		unit.id = tokenizer("", index);
		if (tokenizer("player-id", index) == player_id)   // Our units only
		{	unit.type = tokenizer("type", index);

			unit.coord.x = tokenizer("loc", index);
			unit.coord.y = tokenizer("", index);

			unit.hit_points = tokenizer("hp", index);
			unit.value = tokenizer("r-amt", index);
			unit.status = tokenizer("status", index);

			if (unit.type == 2)
			{	if (unit.value > 0)
				{	if (unit.status == 1 || unit.status == 7)
						unit.resource = (Wargus_State::Resource)tokenizer("status-args", index);
					else
						unit.resource = peasant_previous_state[state().peasants.size()].resource;
				}
				else
					unit.resource = Wargus_State::NL;
			}

			switch (unit.type)
			{	case 2:
					state().peasants.push_back(unit);
					break;
				case 74:
					state().townhalls.push_back(unit);
					break;
				case 92:
					state().goldmines.push_back(unit);
					break;
				default:;
			}
		}
	}

	// Get amount of stored gold & wood
	state().resources_gathered.gold = tokenizer(":gold", index);
	state().resources_gathered.wood = tokenizer(":wood", index);

	// Update internal variables
	state().agent_region_gold.clear();
	state().agent_region_wood.clear();
	state().agent_region_townhall.clear();
	state().agent_region_gold.resize(state().num_agents(), Wargus_State::NA);
	state().agent_region_wood.resize(state().num_agents(), Wargus_State::NA);
	state().agent_region_townhall.resize(state().num_agents(), Wargus_State::NA);
	for (unsigned agent = 0; agent < state().num_agents(); ++agent)
	{	Wargus_State::Region agent_region;
		agent_region.coord = state().peasants[agent].coord;
		agent_region.left_up_offset = Coordinate(min(agent_region.coord.x, state().effective_range), min(agent_region.coord.y, state().effective_range));
		agent_region.right_down_offset = Coordinate(min(state().map_size().x - 1 - agent_region.coord.x, state().effective_range), min(state().map_size().y - 1 - agent_region.coord.y, state().effective_range));

		// Checking for gold (goldmines[g].coord stores the coord of the top left corner of every mine)
		for (const auto& goldmine : state().goldmines)
			if ((goldmine.coord.x <= agent_region.coord.x ? agent_region.coord.x - goldmine.coord.x < agent_region.left_up_offset.x + state().goldmine_width
																: goldmine.coord.x - agent_region.coord.x <= agent_region.right_down_offset.x)
					&& (goldmine.coord.y <= agent_region.coord.y ? agent_region.coord.y - goldmine.coord.y < agent_region.left_up_offset.y + state().goldmine_width
																		: goldmine.coord.y - agent_region.coord.y <= agent_region.right_down_offset.y)
					&& goldmine.value > 0)
				{	state().agent_region_gold[agent] = 1;
					break;
				}
		if (state().agent_region_gold[agent] == Wargus_State::NA)
			state().agent_region_gold[agent] = 0;

		// Checking for wood
		for (int y = agent_region.coord.y - agent_region.left_up_offset.y; y <= agent_region.coord.y + agent_region.right_down_offset.y; ++y)
			for (int x = agent_region.coord.x - agent_region.left_up_offset.x; x <= agent_region.coord.x + agent_region.right_down_offset.x; ++x)
				if (state().map_layout(x, y) == 'T')   // Tree found
				{	state().agent_region_wood[agent] = 1;
					break;
				}
		if (state().agent_region_wood[agent] == Wargus_State::NA)
			state().agent_region_wood[agent] = 0;

		// Checking for town halls (townhall[g].coord stores the coord of the top left corner of every town hall)
		for (const auto& townhall : state().townhalls)
			if ((townhall.coord.x <= agent_region.coord.x ? agent_region.coord.x - townhall.coord.x < agent_region.left_up_offset.x + state().townhall_width
																: townhall.coord.x - agent_region.coord.x <= agent_region.right_down_offset.x)
					&& (townhall.coord.y <= agent_region.coord.y ? agent_region.coord.y - townhall.coord.y < agent_region.left_up_offset.y + state().townhall_width
																		: townhall.coord.y - agent_region.coord.y <= agent_region.right_down_offset.y))
				{	state().agent_region_townhall[agent] = 1;
					break;
				}
		if (state().agent_region_townhall[agent] == Wargus_State::NA)
			state().agent_region_townhall[agent] = 0;
	}
	state().requisite_resources.gold = state().resources_gathered.gold < state().resource_quotas.gold ? 0 : 1;
	state().requisite_resources.wood = state().resources_gathered.wood < state().resource_quotas.wood ? 0 : 1;
}


void Wargus::reset ()
{	socket->send("r\n");
	read_socket(false);
}


void Wargus::quit ()
{	socket->send("q\n");
	while (socket->receive(response));
}


void Wargus::kill ()
{	socket->send("k\n");
}


void Wargus::set_speed (const int& game_speed, const int& cycles_per_video_update)
{	ostringstream out_stream;
	out_stream << "s " << game_speed << endl;
	socket->send(out_stream.str());
	read_socket(false);

	out_stream.str("");
	out_stream << "v " << cycles_per_video_update << endl;
	socket->send(out_stream.str());
	read_socket(false);
}


unsigned Wargus::get_cycle ()
{	socket->send("x\n");
	read_socket(false);
	return from_string<unsigned>(response);
}


void Wargus::transition (const unsigned& num_cycles) const
{	ostringstream out_stream;
	out_stream << "t " << num_cycles << endl;
	socket->send(out_stream.str());
}


void Wargus::noop (const unsigned& unit_id) const
{	ostringstream out_stream;
	out_stream << "c " << unit_id << " 0\n";
	socket->send(out_stream.str());
}


void Wargus::stop (const unsigned& unit_id) const
{	ostringstream out_stream;
	out_stream << "c " << unit_id << " 1\n";
	socket->send(out_stream.str());
}


void Wargus::move (const unsigned& unit_id, const Coordinate& coord) const
{	ostringstream out_stream;
	out_stream << "c " << unit_id << " 2 " << coord.x << " " << coord.y << endl;
	socket->send(out_stream.str());
}


bool Wargus::harvest (const unsigned& unit_id, const unsigned& resource)
{	ostringstream out_stream;
	out_stream << "c " << unit_id << " 6 " << resource << endl;
	socket->send(out_stream.str());
	read_socket(false);

	if (response == "OK\n")
		return true;
	else if (response == "Unable to find gold within range.\n" || response == "Unable to find wood within range.\n")
		return false;
	else
		throw HierException(__FILE__, __LINE__, "Unknown response from Stratagus.");
}


bool Wargus::deposit (const unsigned& unit_id)
{	ostringstream out_stream;
	out_stream << "c " << unit_id << " 7\n";
	socket->send(out_stream.str());
	read_socket(false);

	if (response == "OK\n")
		return true;
	else if (response == "Unit has no resources to return.\n" || response == "Unable to find location to return resource within range.\n")
		return false;
	else
		throw HierException(__FILE__, __LINE__, "Unknown response from Stratagus.");
}


void Wargus::process (const vector<int>& actions)
{	bool all_actions_in_progress = true;
	while (all_actions_in_progress)
	{	transition(30);
		state_update();

		for (unsigned agent = 0; agent < state().num_agents(); ++agent)
		{	if (!state().action_complete(agent))
			{	_duration[agent]++;
				_reward[agent] += reward_default;
			}
			else
				all_actions_in_progress = false;
		}
	}

	for (auto da_itr = depositing_agents.begin(); da_itr != depositing_agents.end(); )
	{	if (state().action_complete(*da_itr))
			da_itr = depositing_agents.erase(da_itr);
		else
			++da_itr;
	}
}


Wargus::~Wargus ()
{	set_speed(100, 1);
	quit();
//	kill();
	delete socket;
}
