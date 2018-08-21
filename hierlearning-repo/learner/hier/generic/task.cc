
/**********************************************************************************

	GENERIC TASK FRAMEWORK
		Neville Mehta

***********************************************************************************/


#include <functional>
#include <numeric>
#include <sstream>
#include "../../../lib/common.h"
#include "task.h"



Task::Task (const string& task_name, const string& parameters, const string& state_variables, const MDP& mdp) : _name(task_name)
{	vector<string> parameter_tokens = tokenize(parameters, ", ");   // Tokenize parameter types separated by ","
	for (const string& str : parameter_tokens)
		_parameter_size.push_back(mdp.state().parse(str).second);

	// Determining the total number of parameter bindings
	_num_bindings = accumulate(_parameter_size.begin(), _parameter_size.end(), 1, multiplies<int>());

	// Tokenize state variables separated by ","
	vector<string> state_variable_tokens = tokenize(state_variables, ", ");
	for (const string& state_variable : state_variable_tokens)
	{	if (state_variable.substr(0, 6) == "agent_" || state_variable.substr(0, 5) == "agent")   // Agent-based
			_variables.emplace_back(1, mdp.state().variable_index(state_variable));
		else if (state_variable.substr(0, 10) == "parameter_")   // Parameter-based
		{	int p = from_string<int>(state_variable.substr(10));
			string::size_type position;
			if (p >= 0 && p < int(_parameter_size.size()) && (position = state_variable.find(':', 10)) != string::npos)
				_variables.emplace_back(2 + p, mdp.state().variable_index(state_variable.substr(position + 1)));
			else
				throw HierException(__FILE__, __LINE__, "Incorrect specification of parameter-based state variable.");
		}
		else
			_variables.emplace_back(0, mdp.state().variable_index(state_variable));
	}

	// Determining the state space size of the task
	_num_states = 1;
	for (const auto& v : _variables)
		_num_states *= mdp.state().variable_size(v.value);
}


set<int> Task::abstraction () const
{	set<int> abstraction;
	for (const auto& v : _variables)
		abstraction.insert(v.value);
	return abstraction;
}


string Task::abstraction_str () const
{	ostringstream out;
	for (auto v_itr = _variables.cbegin(); v_itr != _variables.cend(); ++v_itr)
	{	if (v_itr != _variables.cbegin())
			out << ",";

		switch (v_itr->type)
		{	case 0:   // Regular
				out << "v";
				break;

			case 1:   // Agent-based
				out << "av";
				break;

			default:   // Parameter-based
				out << "p";
		}
		out << v_itr->value;
	}
	return out.str();
}


string Task::abstraction_str_html () const
{	ostringstream out;
	for (auto v_itr = _variables.cbegin(); v_itr != _variables.cend(); ++v_itr)
	{	if (v_itr != _variables.cbegin())
			out << ",";

		switch (v_itr->type)
		{	case 0:   // Regular
				out << "<i>v</i>";
				break;

			case 1:   // Agent-based
				out << "<i>av</i>";
				break;

			default:   // Parameter-based
				out << "<i>p</i>";
		}
		out << "<sub>" << v_itr->value << "</sub>";
	}
	return out.str();
}


int Task::hash (const unsigned& agent, const vector<int>& parameters, const State& state) const
{	int hash = 0;

	// Parameters
	for (int p = int(parameters.size()) - 1; p >= 0; --p)
		hash = _parameter_size[p] * hash + parameters[p];

	// State variables
	for (auto v_itr = _variables.crbegin(); v_itr != _variables.crend(); ++v_itr)
	{	int var;
		switch (v_itr->type)
		{	case 0:   // Regular
				var = v_itr->value;
				break;

			case 1:   // Agent-based
				var = agent * state.num_agent_variables() + v_itr->value;
				break;

			default:   // Parameter-based
				var = parameters[v_itr->type - 2] + v_itr->value;
		}
		hash = state.variable_size(var) * hash + state.variable(var);
	}

	return hash;
}


pair<vector<int>, unique_ptr<State>> Task::unhash (const unsigned& agent, int hash, const State& state) const
{	// Unhash parameters
	vector<int> parameters(_parameter_size.size());
	int par_hash = hash / _num_states;
	for (unsigned p = 0; p < parameters.size(); ++p)
	{	parameters[p] = par_hash % _parameter_size[p];
		par_hash /= _parameter_size[p];
	}

	// Unhash state variables
	unique_ptr<State> _state = state.copy();
	for (const auto& v : _variables)
	{	int var;
		switch (v.type)
		{	case 0:   // Regular
				var = v.value;
				break;

			case 1:   // Agent-based
				var = agent * state.num_agent_variables() + v.value;
				break;

			default:   // Parameter-based
				var = parameters[v.type - 2] + v.value;
		}
		_state->variable(var) = hash % state.variable_size(var);
		hash /= state.variable_size(var);
	}

	return make_pair(parameters, move(_state));
}


bool Task::update_parameters (const vector<int>& parameter_size)
{	if (parameter_size.size() != _parameter_size.size())
		throw HierException(__FILE__, __LINE__, "Differing number of parameters.");

	// Assign the larger size
	bool updated = false;
	for (unsigned p = 0; p < _parameter_size.size(); ++p)
		if (_parameter_size[p] < parameter_size[p])
		{	_parameter_size[p] = parameter_size[p];
			updated = true;
		}

	if (updated)
		_num_bindings = accumulate(_parameter_size.begin(), _parameter_size.end(), 1, multiplies<int>());

	return updated;
}


string Task::print_dot () const
{	ostringstream output;
	output << name() << " [label=<" << name();
	if (num_parameters())
	{	output << "(";
		for (unsigned p = 0; p < num_parameters(); ++p)
		{	if (p > 0)
				output << ",";
			output << "<i>x</i><sub>" << p << "</sub>";
		}
		output << ")";
	}
	output << "<br/>";
	return output.str();
}
