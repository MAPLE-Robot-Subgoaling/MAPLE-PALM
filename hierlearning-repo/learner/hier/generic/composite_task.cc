
/**********************************************************************************

	GENERIC COMPOSITE TASK
		Neville Mehta

***********************************************************************************/


#include <algorithm>
#include "../../../lib/common.h"
#include "composite_task.h"



CompositeTask::CompositeTask (const string& task_name, const string& parameters, const string& state_variables,
					const vector<Subtask>& subtasks, bool (*function)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp)
											: Task(task_name, parameters, state_variables, mdp), _num_subtasks(0), _termination_function(function), _termination_expression(expression)
{	for (const auto& subtask : subtasks)
		add_subtask(subtask, mdp);   // Resize all together at the end
}


void CompositeTask::add_subtask (const Subtask& subtask, const MDP& mdp)
{	vector<string> subtask_parameters_str = tokenize(subtask.parameters, ", ");   // Tokenize the subtask's parameter binding string
	if (subtask_parameters_str.size() == subtask.link->_parameter_size.size())   // #(bindings) = #(parameters)
	{	vector<Parameter> subtask_parameters(subtask_parameters_str.size());
		vector<unsigned> free_parameters;
		for (unsigned p = 0; p < subtask_parameters_str.size(); ++p)
		{	if (subtask_parameters_str[p] == "?")   // Actual parameter binding value stored in the 'value' field
			{	subtask_parameters[p].type = 0;
				subtask_parameters[p].value = 0;
				free_parameters.push_back(p);
			}
			else if (subtask_parameters_str[p].substr(0, 9) == "constant:")   // Parameter binding value is constant = 'value'
			{	subtask_parameters[p].type = 0;
				subtask_parameters[p].value = mdp.state().parse(subtask_parameters_str[p].substr(9, subtask_parameters_str[p].find_first_of("+- "))).second;
			}
			else if (subtask_parameters_str[p].substr(0, 10) == "parameter:")   // Parameter binding value comes from the parent task's parameter indexed by 'value'
			{	subtask_parameters[p].type = 1;
				istringstream par(subtask_parameters_str[p].substr(10));
				if (!(par >> subtask_parameters[p].value) || subtask_parameters[p].value < 0 || subtask_parameters[p].value >= int(_parameter_size.size()))
					throw HierException(__FILE__, __LINE__, "Unknown parameter specification.");
			}
			else if (subtask_parameters_str[p].substr(0, 6) == "agent_")   // Parameter binding value comes from the agent's state variable indexed by 'value'
			{	subtask_parameters[p].type = 2;
				subtask_parameters[p].value = mdp.state().variable_index(subtask_parameters_str[p].substr(0, subtask_parameters_str[p].find_first_of("+- ")));
			}
			else   // Parameter binding value comes from the state variable indexed by 'value'
			{	subtask_parameters[p].type = 3;
				subtask_parameters[p].value = mdp.state().variable_index(subtask_parameters_str[p].substr(0, subtask_parameters_str[p].find_first_of("+- ")));
			}

			// Offset
			subtask_parameters[p].offset = (subtask_parameters_str[p].find_first_of("+-") != string::npos) ?
												mdp.state().parse(subtask_parameters_str[p].substr(subtask_parameters_str[p].find_first_of("+-"))).second : 0;
		}

		_subtask.emplace_back(subtask.link, subtask_parameters);
		++_num_subtasks;

		// All possible bindings permutations for the free subtask parameters
		while (free_parameters.size())   // Free parameters exist
		{	unsigned fp = 0;
			++subtask_parameters[free_parameters[fp]].value;
			while (subtask_parameters[free_parameters[fp]].value == subtask.link->_parameter_size[free_parameters[fp]] && fp < free_parameters.size() - 1)
			{	subtask_parameters[free_parameters[fp]].value = 0;   // Reset its value
				++subtask_parameters[free_parameters[++fp]].value;   // Increment the next free parameter
			}
			if (subtask_parameters[free_parameters.back()].value == subtask.link->_parameter_size[free_parameters.back()])
				break;   // All permutations have been exhausted

			// Insert new parameter combo into the subtask data structure
			_subtask.emplace_back(subtask.link, subtask_parameters);
			++_num_subtasks;
		}
	}
	else
		throw HierException(__FILE__, __LINE__, "The parameter and binding lists aren't of the same size.");
}


vector<int> CompositeTask::subtask_bindings (const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t) const
{	vector<int> subtask_bindings(_subtask[t].parameters.size());

	for (unsigned p = 0; p < subtask_bindings.size(); ++p)
	{	switch (_subtask[t].parameters[p].type)
		{	case 0:   // Actual value
				subtask_bindings[p] = _subtask[t].parameters[p].value;
				break;

			case 1:   // Parameter value
				subtask_bindings[p] = parameters[_subtask[t].parameters[p].value];
				break;

			case 2:   // Agent-dependent state variable
				subtask_bindings[p] = state.variable(agent * state.num_agent_variables() + _subtask[t].parameters[p].value);
				break;

			case 3:   // State variable
				subtask_bindings[p] = state.variable(_subtask[t].parameters[p].value);
				break;
		}
		subtask_bindings[p] += _subtask[t].parameters[p].offset;

		if (subtask_bindings[p] < 0 || subtask_bindings[p] >= _subtask[t].link->_parameter_size[p])
			throw ParameterException(__FILE__, __LINE__, "Parameter binding out of range.");
	}

	return subtask_bindings;
}


string CompositeTask::binding_str (const unsigned& t) const
{	if (_subtask[t].parameters.empty())
		return "";

	ostringstream out;
	for (unsigned p = 0; p < _subtask[t].parameters.size(); ++p)
	{	if (p > 0)
			out << ",";

		switch (_subtask[t].parameters[p].type)
		{	case 0:   // Actual value
				break;

			case 1:   // Parameter value
				out << "x";
				break;

			case 2:   // Agent-dependent state variable
				out << "av";
				break;

			case 3:   // State variable
				out << "v";
				break;
		}
		out << _subtask[t].parameters[p].value;
	}
	return out.str();
}


string CompositeTask::binding_str_html (const unsigned& t) const
{	if (_subtask[t].parameters.empty())
		return "";

	ostringstream out;
	for (unsigned p = 0; p < _subtask[t].parameters.size(); ++p)
	{	if (p > 0)
			out << ",";

		switch (_subtask[t].parameters[p].type)
		{	case 0:   // Actual value
				break;

			case 1:   // Parameter value
				out << "<i>x</i>";
				break;

			case 2:   // Agent-dependent state variable
				out << "<i>av</i>";
				break;

			case 3:   // State variable
				out << "<i>v</i>";
				break;
		}
		out << "<sub>" << _subtask[t].parameters[p].value << "</sub>";
	}
	return out.str();
}


vector<unsigned> CompositeTask::admissible_subtasks (const unsigned& agent, const vector<int>& parameters, const State& state) const
{	vector<unsigned> adm_tasks;

	/* A subtask is admissible if:
		1. It is primitive.
		  OR
		1. The subtask's parameters can be grounded legally.
		2. The subtask is not terminated. */
	for (unsigned t = 0; t < _num_subtasks; ++t)
		if (_subtask[t].link->admissible(agent, subtask_bindings(agent, parameters, state, t), state))
			adm_tasks.push_back(t);

	if (!adm_tasks.empty())
		return adm_tasks;

	throw HierException(__FILE__, __LINE__, _name + " has no admissible children.");
}


unsigned CompositeTask::exploratory_policy (const unsigned& agent, const vector<int>& parameters, const State& state)
{	vector<unsigned> adm_tasks = admissible_subtasks(agent, parameters, state);
	unsigned num_admissible_tasks = adm_tasks.size();
	if (num_admissible_tasks == 1)
		return adm_tasks[0];
	
	if (rand_real() < composite_task_parameters::EPSILON)   // Random
		return adm_tasks[rand_int(num_admissible_tasks)];
	else
		return greedy_policy(agent, parameters, state);
}


bool CompositeTask::terminated (const unsigned& agent, const vector<int>& parameters, const State& state) const
{	if (_termination_function)
		return _termination_function(agent, parameters, state);
	else
		return _termination_expression.evaluate(state, parameters, agent) != 0;
}


string CompositeTask::print_dot () const
{	string output = Task::print_dot() + termination_expression().print_html() + "<br/>{" + abstraction_str_html() + "}>]\n";

	// Child tasks
	for (unsigned t = 0; t < num_subtasks(); ++t)
	{	output += name() + " -> " + get_subtask(t)->name();
		string binding_str = binding_str_html(t);
		if (!binding_str.empty())
			output += " [label=<" + binding_str + ">,fontsize=10]";
		output += "\n";
	}

	return output;
}
