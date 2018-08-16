
/**********************************************************************************

	FUNCTION
		Neville Mehta

***********************************************************************************/


#include "../../../lib/common.h"
#include "function.h"


Function::Function (const string& state_variables, const unsigned& num_actions, const MDP& mdp) : _num_actions(num_actions)
{	vector<string> state_variable_tokens = tokenize(state_variables, ", ");
	for (const auto& state_variable_token : state_variable_tokens)
	{	if (state_variable_token.substr(0, 6) == "agent_")   // Agent-based
			variables.emplace_back(1, mdp.state().variable_index(state_variable_token));
		else
			variables.emplace_back(0, mdp.state().variable_index(state_variable_token));
	}

	// Number of states
	_num_states = 1;
	for (const auto& v : variables)
		_num_states *= mdp.state().variable_size(v.second);
}


int Function::hash (const unsigned& agent, const State& state) const
{	int hash = 0;

	// State variables
	for (auto v = variables.crbegin(); v != variables.crend(); ++v)
	{	int var;
		switch (v->first)
		{	case 0:   // Regular
				var = v->second;
				break;

			case 1:   // Agent-based
				var = agent * state.num_agent_variables() + v->second;
				break;
		}
		hash = state.variable_size(var) * hash + state.variable(var);
	}

	return hash;
}


unique_ptr<State> Function::unhash (const unsigned& agent, int hash, const State& state) const
{	unique_ptr<State> _state(state.copy());

	// Unhash state variables
	for (const auto& v : variables)
	{	int var;
		switch (v.first)
		{	case 0:   // Regular
				var = v.second;
				break;

			case 1:   // Agent-based
				var = agent * state.num_agent_variables() + v.second;
				break;
		}
		_state->variable(var) = hash % state.variable_size(var);
		hash /= state.variable_size(var);
	}

	return _state;
}


unsigned Function::exploratory_policy (const unsigned& agent, const State& state)
{	if (rand_real() < function_parameters::EPSILON)
		return rand_int(_num_actions);
	else
		return greedy_policy(agent, state);
}
