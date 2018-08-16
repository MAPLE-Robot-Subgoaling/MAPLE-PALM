
/*************************************************

	Cleanup DOMAIN

**************************************************/


#pragma once


#include <iostream>
#include <map>
#include <set>
#include <vector>
#include "../../lib/common.h"
#include "../../lib/matrix.h"
#include "../mdp.h"

static const int num_state_classes = 4;
static const int state_classes_name_lengths[] = {5, 5, 4, 4};
static const char *state_classes[] = {"agent", "block", "door", "room"};

static const int state_classes_num_variables[] = {9, 8, 9, 6};
static const char *agent_variables[] = {"x","y","left","right","bottom","top","shape","color","direction"};
static const char *block_variables[] = {"x","y","left","right","bottom","top","shape","color"};
static const char *door_variables[] = {"x","y","left","right","bottom","top","shape","color","locked"};
static const char *room_variables[] = {"left","right","bottom","top","shape","color"};

struct Cleanup_State : public State
{	
	Coordinate map_size;
	int num_variables;
	int num_blocks;
	int num_rooms;
	int num_doors;
	//enum StateClass {agent, block, door, room};
	enum Shape {chair, bag, backpack, basket};
	enum Color {blue, green, magenta, red, yellow};
	enum Direction {north, south, east, west};

	struct Agent
	{	
		int x;
		int y;
		int left;
		int right;
		int bottom;
		int top;
		Shape shape;
		Color color;
		Direction direction;
	} agent;
	static const int num_agent_variables = 9;

	struct Block
	{
		int x;
		int y;
		int left;
		int right;
		int bottom;
		int top;
		Shape shape;
		Color color;
	};
	vector<Block> blocks;
	static const int num_block_variables = 8;
	
	struct Door
	{
		int x;
		int y;
		int left;
		int right;
		int bottom;
		int top;
		Shape shape;
		Color color;
		int locked;
	};
	vector<Door> doors;
	static const int num_door_variables = 9;

	struct Room
	{
		int left;
		int right;
		int bottom;
		int top;
		Shape shape;
		Color color;
	};
	vector<Room> rooms;
	static const int num_room_variables = 6;

	Cleanup_State(
		const Coordinate& map_size = Coordinate(9,9),
		const int& num_blocks = 1,
		const int& num_doors = 1,
		const int& num_rooms = 2
	);
	unique_ptr<State> clone () const { return unique_ptr<State>(new Cleanup_State(*this)); }
	unique_ptr<State> copy () const { return unique_ptr<State>(new Cleanup_State(map_size, num_blocks, num_doors, num_rooms)); }
	vector<int> variables() const;
	int variable_index(const string& variable) const;
	const char *get_variables(string state_class_test) const;
	int get_num_objects(string state_class_test) const;
	string variable_name(const int& variable_index) const;
	int variable_size(const int& variable_index) const;
	int variable(const int& variable_index) const;
	int& variable(const int& variable_index);
	map<int,int> variables_mapper() const;
	pair<bool,int> parse(string expression) const;
	void read(istream& in);
	string print() const;
};


/// State, reward, and time structure for the environment
class Cleanup : public MDP
{	protected:
		const double reward_default;   ///< Default reward
		const double reward_goal;   ///< For correct drop-offs
		const double reward_pull;   ///< 
		const double reward_noop;
		const double Pr_successful_execution;   ///< Probability that action is successful

		void map_creator(const int& mode = 0);

	public:
		enum Action {north, south, east, west, pull};

		Cleanup(const string& name, const double& success_probability);
		const Cleanup_State& state () const { return *static_cast<Cleanup_State*>(_state); }   // Covariant return type for easy access
		Cleanup_State& state () { return *static_cast<Cleanup_State*>(_state); }   // Covariant return type for easy access
		vector<int> actions () const { return make_vector<int>(5, north, south, east, west, pull); }
		void initialize(const bool& target = false);
		virtual void process(const vector<int>& action);
		bool terminated() const;
		int action_index(const string& action_name) const;
		string print_action(const int& action) const;
		virtual ~Cleanup () {}
};
