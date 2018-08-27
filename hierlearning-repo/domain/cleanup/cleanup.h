
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

static const int state_classes_num_variables[] = {3, 4, 2, 5};
static const char *agent_variables[] = {"x","y","direction"};
static const char *block_variables[] = {"x","y","shape","color"};
static const char *door_variables[] = {"x","y"};
static const char *room_variables[] = {"left","right","bottom","top","color"};

static const int min_x = 0;
static const int min_y = 0;
static const int max_x = 7;
static const int max_y = 7;

enum Shape {chair, bag, backpack, basket};
static const int num_shapes = 4;

enum Color {blue, green, red, yellow, magenta};//, cyan, orange};
static const int num_colors = 5;

enum Direction {north, south, east, west};
static const Direction directions[] = {north, south, east, west};
static const int num_directions = 4;

static const Color room_colors[] = {blue, green, red, yellow, magenta};//, cyan, orange};
static const int num_room_colors = 5;

static const Color block_colors[] = {blue, green, red, yellow, magenta};
static const int num_block_colors = 5;

static const Shape block_shapes[] = {chair, bag, backpack, basket};
static const int num_block_shapes = 4;

static const Shape goal_block_shapes[] = {bag, backpack};
static const int num_goal_block_shapes = 2;

static const Shape nongoal_block_shapes[] = {chair, basket};
static const int num_nongoal_block_shapes = 2;

struct Cleanup_State : public State
{	
	Coordinate map_size;
	int num_variables;
	int num_blocks;
	int num_rooms;
	int num_doors;
	//enum StateClass {agent, block, door, room};

	struct Agent
	{	
		Coordinate position;
		Direction direction;
	} agent;
	static const int num_agent_variables_data = 3;

	struct Block
	{
		Coordinate position;
		Shape shape;
		Color color;
	};
	vector<Block> blocks;
	static const int num_block_variables = 4;
	
	struct Door
	{
		Coordinate position;
	};
	vector<Door> doors;
	static const int num_door_variables = 2;

	struct Room
	{
		int left;
		int right;
		int bottom;
		int top;
		Color color;
	};
	vector<Room> rooms;
	static const int num_room_variables = 5;

	Cleanup_State(
		const Coordinate& map_size = Coordinate(max_x, max_y),
		const int& num_blocks = 3,
		const int& num_doors = 3,
		const int& num_rooms = 3
	);
	unique_ptr<State> clone () const { return unique_ptr<State>(new Cleanup_State(*this)); }
	unique_ptr<State> copy () const { return unique_ptr<State>(new Cleanup_State(map_size, num_blocks, num_doors, num_rooms)); }
	vector<int> variables() const;
	int debug_num_states() const;
	int variable_index(const string& variable) const;
	const char **get_variables(string state_class_test) const;
	int get_num_objects(string state_class_test) const;
	string variable_name(const int& variable_index) const;
	int variable_size(const int& variable_index) const;
	int variable_size_for_state(const int& variable_index) const;
	int variable(const int& variable_index) const;
	int& variable(const int& variable_index);
	map<int,int> variables_mapper() const;
	pair<bool,int> parse(string expression) const;
	unsigned num_agent_variables () const { return num_agent_variables_data; }
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
		void three_rooms();
		bool is_all_inside_room_of_same_color(Shape shape) const;
		bool is_inside_room_of_same_color(int block_index) const;
		bool is_inside_room_of_same_color(Cleanup_State::Block block) const;
		bool is_inside(int block_index, int room_index) const;
		bool is_inside(Cleanup_State::Block block, int room_index) const;
		bool is_inside(Cleanup_State::Block block, Cleanup_State::Room room) const;
		void do_move(int dx, int dy);
		void do_pull();
		bool is_open(int x, int y);
		bool wall_at(int x, int y);
		bool door_at(int x, int y);
		bool block_at(int x, int y) const;
		bool Cleanup::agent_at(int x, int y) const;

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