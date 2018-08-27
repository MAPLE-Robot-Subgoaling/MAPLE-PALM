
/*************************************************

	MARKOV DECISION PROCESS
		Neville Mehta

**************************************************/


#pragma once


#include <map>
#include <memory>
#include <set>
#include <string>
#include <valarray>
#include <vector>
using namespace std;


struct State
{	State () {}
	virtual unique_ptr<State> clone() const = 0;   // Exact replicate
	virtual unique_ptr<State> copy() const = 0;   // Reinitialized variables
	virtual unsigned num_agents () const { return 1; }
	virtual vector<int> variables() const = 0;
	virtual int debug_num_states() const = 0;
	virtual int variable_index(const string& variable) const = 0;
	virtual string variable_name(const int& variable_index) const = 0;
	virtual int variable_size(const int& variable_index) const = 0;
	virtual int variable_size_for_state(const int& variable_index) const = 0;
	virtual int variable(const int& variable_index) const = 0;
	virtual int& variable(const int& variable_index) = 0;
	virtual map<int,int> variables_mapper() const = 0;
	virtual unsigned num_agent_variables() const = 0;
	virtual set<int> goal_variables() const { return set<int>(); }
	virtual set<int> transfer_variables() const { return set<int>(); }
	virtual pair<bool,int> parse(string expression) const = 0;
	virtual bool action_complete (const unsigned& agent) const { return true; }   // Completion of temporally extended action in semi-MDPs
	virtual void read(istream& in) = 0;
	virtual string print() const = 0;
	virtual ~State () {}
};


// MDP structure for the environment
class MDP
{	protected:
		string _name;
		State* const _state;
		valarray<double> _reward;
		valarray<double> _duration;

	public:
		MDP (const string& name, State* const state)
				: _name(name), _state(state), _reward(valarray<double>(0.0, _state->num_agents())), _duration(valarray<double>(0.0, _state->num_agents())) {}
		string name () const { return _name; }
		virtual const State& state() const { return *_state; }
		virtual State& state() { return *_state; }
		virtual vector<int> actions() const = 0;
		valarray<double> reward () const { return _reward; }
		valarray<double> duration () const { return _duration; }
		virtual void initialize(const bool& target = false) = 0;
		virtual void process(const vector<int>& action) = 0;   // Signals episode termination
		virtual bool terminated() const = 0;
		virtual int action_index(const string& action_name) const = 0;
		virtual string print_action(const int& action) const = 0;
		virtual ~MDP () { delete _state; }
};


struct State_Action_Reward
{	unique_ptr<State> state;
	vector<int> action;
	valarray<double> reward;

	State_Action_Reward (const State& state, const vector<int>& action) : state(state.clone()), action(action), reward(valarray<double>(0.0, action.size())) {}
	~State_Action_Reward () {}
};
