
/**********************************************************************************

	MAXQ COMPOSITE TASK
		Neville Mehta

***********************************************************************************/


#include <limits>
#include <numeric>
#include "../../../lib/common.h"
#include "maxq_composite_task.h"



MaxQ_CompositeTask::MaxQ_CompositeTask (const string& task_name, const string& parameters, const string& state_variables,
					const vector<Subtask>& subtasks, bool (*function)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp)
							: Task(task_name, parameters, state_variables, mdp), CompositeTask(task_name, parameters, state_variables, subtasks, function, expression, mdp), MaxQ_Task(task_name, parameters, state_variables, mdp)
{	// Create the C(s,a) table
	_C.resize(_num_bindings * _num_states, _num_subtasks);
}


void MaxQ_CompositeTask::add_subtask (const Subtask& subtask, const MDP& mdp)
{	CompositeTask::add_subtask(subtask, mdp);
	_C.resize(_num_bindings * _num_states, _num_subtasks);
}


bool MaxQ_CompositeTask::update_parameters (const vector<int>& parameter_size)
{	if (Task::update_parameters(parameter_size))
	{	_C.resize(_num_bindings * _num_states, _num_subtasks);
		return true;
	}
	return false;
}


void MaxQ_CompositeTask::initialize ()
{	// Initializing the C(s,a) table
	_C = 0.0;
}


inline double MaxQ_CompositeTask::V (const unsigned& agent, const vector<int>& parameters, const State& state) const
{	if (terminated(agent, parameters, state))
		return 0.0;
	else
	{	vector<unsigned> adm_tasks = admissible_subtasks(agent, parameters, state);
		int s = hash(agent, parameters, state);
		double max_val = -numeric_limits<double>::max();
	
		for (auto t = adm_tasks.cbegin(); t != adm_tasks.cend(); ++t)
		{	double eqn = dynamic_cast<MaxQ_Task*>(_subtask[*t].link)->V(agent, subtask_bindings(agent, parameters, state, *t), state) + _C(s, *t);
			if (max_val < eqn)
				max_val = eqn;
		}

		return max_val;
	}
}


unsigned MaxQ_CompositeTask::greedy_policy (const unsigned& agent, const vector<int>& parameters, const State& state)
{	vector<unsigned> adm_tasks = admissible_subtasks(agent, parameters, state);
	if (adm_tasks.size() == 1)
		return adm_tasks[0];
	else
	{	double max_val = -numeric_limits<double>::max();
		int s = hash(agent, parameters, state);

		vector<unsigned> gtasks;
		for (auto t = adm_tasks.cbegin(); t != adm_tasks.cend(); ++t)
		{	double eqn = dynamic_cast<MaxQ_Task*>(_subtask[*t].link)->V(agent, subtask_bindings(agent, parameters, state, *t), state) + _C(s, *t);
			if (max_val < eqn)
			{	max_val = eqn;

				// Initiate the list of greedy subtasks
				gtasks.clear();
				gtasks.push_back(*t);
			}
			else if (max_val == eqn)
				// Add to the list of greedy subtasks
				gtasks.push_back(*t);
		}

		unsigned num_greedy_actions = gtasks.size();
		return gtasks[num_greedy_actions > 1 ? rand_int(num_greedy_actions) : 0];
	}
}


void MaxQ_CompositeTask::update (const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t, const State& next_state, const double& N)
{	int s = hash(agent, parameters, state);
	_C(s, t) += maxq_composite_task::ALPHA * (pow(maxq_composite_task::GAMMA, N) * V(agent, parameters, next_state) - _C(s, t));   // Regular MaxQ-0 update
}


string MaxQ_CompositeTask::print (const State& state) const
{	ostringstream out;
	for (unsigned b = 0; b < _num_bindings; ++b)
	{	out << "\n\n******** " << _name;
		auto parameters_and_state = unhash(0, b * _num_states, state);
		if (!parameters_and_state.first.empty())
			out << parameters_and_state.first;
		out << " ********\n";

		out << "\nC(s,a):\n";
		for (unsigned s = b * _num_states; s < (b + 1) * _num_states; ++s)
			for (unsigned t = 0; t < _num_subtasks; ++t)
				if (_C(s, t) != 0.0)
				{	parameters_and_state = unhash(0, s, state);
					out << "[" << parameters_and_state.second->print() << "], " << _subtask[t].link->name();
					if (!_subtask[t].link->primitive())
					{	try
						{	out << subtask_bindings(0, parameters_and_state.first, *parameters_and_state.second, t);
						}
						catch (const ParameterException&)
						{	out << "(" << binding_str(t) << ")";
						}
					}
					out << "   " << _C(s, t) << endl;
				}
	}

	return out.str();
}
