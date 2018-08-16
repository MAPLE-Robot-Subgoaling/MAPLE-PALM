
/**********************************************************************************

	FUNCTION HEADER
		Neville Mehta

***********************************************************************************/


#pragma once

#include <memory>
#include "../../../domain/mdp.h"


namespace function_parameters {
const double EPSILON = 0.1;   // Exploration factor
}


class Function
{	typedef pair<int,int> Variable;
	list<Variable> variables;

	protected:
		unsigned _num_states;
		unsigned _num_actions;

	public:
		Function(const string& state_variables, const unsigned& num_actions, const MDP& mdp);
		int hash (const unsigned& agent, const State& state) const;
		unique_ptr<State> unhash(const unsigned& agent, int hash, const State& state) const;
		virtual void initialize() = 0;
		virtual unsigned greedy_policy(const unsigned& agent, const State& state) = 0;
		virtual unsigned exploratory_policy(const unsigned& agent, const State& state);
		virtual void update(const unsigned& agent, const State& state, const unsigned& action, const double& reward, const double& duration, const State& next_state) = 0;
		virtual string print(const State& state) const = 0;
		virtual ~Function () {}
};
