
/*************************************************

	WARGUS DOMAIN
		Neville Mehta

**************************************************/


#pragma once

#include <iostream>
#include <list>
#include <map>
#include <set>
#include <valarray>
#include <vector>
#include "../../lib/common.h"
#include "../../lib/matrix.h"
#include "client/socket.h"
#include "../mdp.h"


struct Wargus_State : public State
{	const unsigned num_global_variables;   // Number of agent-independent variables {requisite_gold, requisite_gold}
	const int effective_range;   // Unit's effective range
	const int goldmine_width;   // Gold mine occupies 3 x 3 cells on the map
	const int townhall_width;   // Town hall occupies 4 x 4 cells on the map

	enum Resource {NA = -1, NL, Gold, Wood, Oil};
	const int num_resources;

	struct Unit
	{	unsigned type;
		unsigned id;
		Coordinate coord;
		unsigned hit_points;
		int resource;
		unsigned value;
		unsigned status;

		Unit () : type(NA), id(NA), coord(NA), hit_points(NA), resource(NA), value(NA), status(NA) {}
	};
	vector<Unit> peasants;
	vector<Unit> goldmines;
	vector<Unit> townhalls;
	matrix<char> map_layout;

	struct Region
	{	Coordinate coord;
		Coordinate left_up_offset;
		Coordinate right_down_offset;
	};
	matrix<Region> regions;

	// Binary indicator variables
	vector<int> agent_region_gold;
	vector<int> agent_region_wood;
	vector<int> agent_region_townhall;

	struct Resources
	{	int gold;
		int wood;
		int oil;
		Resources () : gold(NA), wood(NA), oil(NA) {}
		Resources (const int& gold, const int& wood) : gold(gold), wood(wood), oil(NA) {}
		void clear () { gold = NA; wood = NA; oil = NA; }
	};
	Resources resources_gathered;   // Numbers currently gathered
	Resources resource_quotas;   // Numbers required for the episode
	Resources requisite_resources;   // Binary state variables

	Wargus_State(const unsigned& num_agents);
	unique_ptr<State> clone () const { return unique_ptr<State>(new Wargus_State(*this)); }
	unique_ptr<State> copy () const { return unique_ptr<State>(new Wargus_State(num_agents())); }
	unsigned num_agents () const { return peasants.size(); }
	vector<int> variables() const;
	int variable_index(const string& variable_name) const;
	string variable_name(const int& variable_index) const;
	int variable_size(const int& variable_index) const;
	int variable(const int& variable_index) const;
	int& variable(const int& variable_index);
	map<int,int> variables_mapper() const;
	unsigned num_agent_variables () const { return 6; }   // {x, y, resource, gold_in_region, wood_in_region, townhall_in_region}
	set<int> goal_variables () const { return make_set<int>(2, variable_index("requisite_gold"), variable_index("requisite_wood")); }
	set<int> transfer_variables () const { return make_set<int>(6, variable_index("agent_resource"), variable_index("agent_region_gold"),
						variable_index("agent_region_wood"), variable_index("agent_region_townhall"), variable_index("requisite_gold"), variable_index("requisite_wood")); }
	bool action_complete(const unsigned& agent) const;
	Coordinate map_size() const;
	void draw_map() const;
	pair<bool,int> parse(string expression) const;
	void read(istream& in);
	string print() const;
};


class Wargus : public MDP
{	protected:
		ClientSocket* socket;
		string response;
		int player_id;
		string source_map_name;
		unsigned num_source_maps;
		Wargus_State::Resources source_resource_quotas;
		string target_map_name;
		unsigned num_target_maps;
		Wargus_State::Resources target_resource_quotas;
		bool target_map_initialized;
		list<int> depositing_agents;
		const int reward_default;
		const int reward_deposit;

		void read_socket(const bool parentheses_matching, const unsigned& num_lines = 1);
		int tokenizer(const string& search_str, string::size_type& index) const;
		void load_map(const string& map_filename);
		void read_map();
		void state_update();
		void reset();
		void quit();
		void kill();
		void set_speed(const int& game_speed, const int& cycles_per_video_update);
		unsigned get_cycle();
		void transition(const unsigned& num_cycles) const;
		void noop(const unsigned& unit_id) const;
		void stop(const unsigned& unit_id) const;
		void move(const unsigned& unit_id, const Coordinate& coord) const;
		bool harvest(const unsigned& unit_id, const unsigned& resource);
		bool deposit(const unsigned& unit_id);

	public:
		Wargus(const string& name, ClientSocket* const socket, const string& source_map_name, const unsigned& num_source_maps, const Wargus_State::Resources& source_resource_quotas, const string& target_map_name, const unsigned& num_target_maps, const Wargus_State::Resources& target_resource_quotas, const int& speed, const int& cycles);
		const Wargus_State& state () const { return *static_cast<Wargus_State*>(_state); }   // Covariant return type for easy access
		Wargus_State& state () { return *static_cast<Wargus_State*>(_state); }   // Covariant return type for easy access
		valarray<double> reward() const;
		virtual void initialize(const bool& target = false);
		virtual void process(const vector<int>& actions);
		bool terminated () const { return state().resources_gathered.gold >= state().resource_quotas.gold && state().resources_gathered.wood >= state().resource_quotas.wood && depositing_agents.empty(); }
		virtual ~Wargus ();
};
