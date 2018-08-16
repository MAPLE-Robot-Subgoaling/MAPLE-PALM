
/*************************************************

	Cleanup DOMAIN

**************************************************/

/*------------------------------------------------------------------------

        WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
       W     |     W     |     |     W
     4 W  R  |     W     |     |  G  W
       W_____|_____W_____|_____|_____W
       W     |     W     |     |     W
     3 W     |     W     |     |     W
       W_____|_____W_____|_____|_____W
       W     |     |     |     |     W
     2 W     |Clup |     |     |     W
       W_____|_____|_____|_____|_____W
       W     W     |     W     |     W
     1 W     W     |     W     |     W
       W_____W_____|_____W_____|_____W
       W     W     |     W     |     W
     0 W  Y  W     |  S  W  B  |     W
       W     W     |     W     |     W
       WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
          0     1     2     3     4

	R, G, B, Y, (S) => Pick-up/Drop-off locations

	State variables: Cleanup's location, fuel; Passenger's location; Passenger's destination
	Actions: North, South, East, West, Pick-up, Drop-off, Wait, Refuel
	Rewards: Successful drop-off -> Reward_Dropoff = 20
	         Illegal pick-up/drop-off -> Reward_Illegal = -10
	         Every action -> Reward_Default = -1

-------------------------------------------------------------------------*/


#pragma once


#include <iostream>
#include <map>
#include <set>
#include <vector>
#include "../../lib/common.h"
#include "../../lib/matrix.h"
#include "../mdp.h"


struct Cleanup_State : public State
{	
	Coordinate map_size;
	int num_blocks;
	enum Shape {chair, bag, backpack, basket};
	enum Color {blue, green, magenta, red, yellow};
	enum Direction {north, south, east, west};

	struct Agent
	{	
		Coordinate location;
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
		Coordinate location;
		int left;
		int right;
		int bottom;
		int top;
		Shape shape;
		Color color;
	};
	vector<Block> blocks;
	static const int num_block_variables = 8;

	Cleanup_State(const Coordinate& map_size = Coordinate(9,9), const int& num_blocks = 1);
	unique_ptr<State> clone () const { return unique_ptr<State>(new Cleanup_State(*this)); }
	unique_ptr<State> copy () const { return unique_ptr<State>(new Cleanup_State(map_size, num_blocks)); }
	vector<int> variables() const;
	int variable_index(const string& variable) const;
	string variable_name(const int& variable_index) const;
	int variable_size(const int& variable_index) const;
	int variable(const int& variable_index) const;
	int& variable(const int& variable_index);
	map<int,int> variables_mapper() const;
	unsigned num_agent_variables () const { return num_agent_variables; }
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
