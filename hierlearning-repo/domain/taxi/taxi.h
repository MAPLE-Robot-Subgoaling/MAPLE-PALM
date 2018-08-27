
/*************************************************

	TAXI DOMAIN
		Neville Mehta

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
     2 W     |Taxi |     |     |     W
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

	State variables: Taxi's location, fuel; Passenger's location; Passenger's destination
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


struct Taxi_State : public State
{	int num_variables;
	Coordinate map_size;
	enum Location {NA = -2, NL, Red, Green, Blue, Yellow, Station, In_taxi};
	vector<Coordinate> site_location;
	int num_sites;
	int num_passengers;
	int taxi_capacity;
	int fuel_max;

	struct Taxi
	{	Coordinate location;
		int fuel;
	} taxi;
	static const int num_taxi_variables = 3;

	struct Passenger
	{	Coordinate location;
		Coordinate destination;
		int in_taxi;   ///< Indicates passenger is in the taxi
	};
	vector<Passenger> passengers;
	static const int num_passenger_variables = 5;
	static const int num_passenger_accessors = 2;

	Taxi_State(const Coordinate& map_size = Coordinate(5,5), const int& num_passengers = 1, const int& taxi_capacity = 1, const int& fuel_max = 0);
	unique_ptr<State> clone () const { return unique_ptr<State>(new Taxi_State(*this)); }
	unique_ptr<State> copy () const { return unique_ptr<State>(new Taxi_State(map_size, num_passengers, taxi_capacity, fuel_max)); }
	vector<int> variables() const;
	int debug_num_states() const;
	int variable_index(const string& variable) const;
	string variable_name(const int& variable_index) const;
	int variable_size(const int& variable_index) const;
	int variable_size_for_state(const int& variable_index) const;
	int variable(const int& variable_index) const;
	int& variable(const int& variable_index);
	map<int,int> variables_mapper() const;
	unsigned num_agent_variables () const { return num_taxi_variables; }
	pair<bool,int> parse(string expression) const;
	void read(istream& in);
	string print() const;
};


/// State, reward, and time structure for the environment
class Taxi : public MDP
{	protected:
		const double reward_default;   ///< Default reward
		const double reward_dropoff;   ///< For correct drop-offs
		const double reward_illegal;   ///< For incorrect pick-ups & drop-offs
		const double Pr_successful_execution;   ///< Probability that action is successful
		const bool noisy;   ///< Navigation to left/right of intention
		matrix<bool> map_walls;

		void map_creator(const int& mode = 0);

	public:
		enum Action {North, South, East, West, Pickup, Dropoff, Refuel, Wait};

		Taxi(const string& name, const double& success_probability);
		const Taxi_State& state () const { return *static_cast<Taxi_State*>(_state); }   // Covariant return type for easy access
		Taxi_State& state () { return *static_cast<Taxi_State*>(_state); }   // Covariant return type for easy access
		vector<int> actions () const { return make_vector<int>(6, North, South, East, West, Pickup, Dropoff); }
		void initialize(const bool& target = false);
		virtual void process(const vector<int>& action);
		bool terminated() const;
		int action_index(const string& action_name) const;
		string print_action(const int& action) const;
		virtual ~Taxi () {}
};
