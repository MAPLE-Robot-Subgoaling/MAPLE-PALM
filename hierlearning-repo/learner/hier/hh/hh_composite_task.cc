
/***********************************************************************

   	HIERARCHICAL H COMPOSITE TASK
		Neville Mehta

************************************************************************/


#include <algorithm>
#include <limits>
#include "../../../lib/common.h"
#include "hh_composite_task.h"



HH_CompositeTask::HH_CompositeTask (const string& task_name, const string& parameters, const string& state_variables,
					const vector<Subtask>& subtasks, bool (*function)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp)
											: Task(task_name, parameters, state_variables, mdp), CompositeTask(task_name, parameters, state_variables, subtasks, function, expression, mdp), HH_Task(task_name, parameters, state_variables, mdp)
{	// Create the N(s,a) and  N(s,a,s') tables
	_Nsa.resize(_num_bindings * _num_states, _num_subtasks);
	_Nsas.resize(_num_bindings * _num_states, _num_subtasks);
}


void HH_CompositeTask::add_subtask (const Subtask& subtask, const MDP& mdp)
{	CompositeTask::add_subtask(subtask, mdp);
	_H.resize(_num_bindings * _num_states);
	_Time.resize(_num_bindings * _num_states);
	_Nsa.resize(_num_bindings * _num_states, _num_subtasks);
	_Nsas.resize(_num_bindings * _num_states, _num_subtasks);
}


bool HH_CompositeTask::update_parameters (const vector<int>& parameter_size)
{	if (Task::update_parameters(parameter_size))
	{	_H.resize(_num_bindings * _num_states);
		_Time.resize(_num_bindings * _num_states);
		_Nsa.resize(_num_bindings * _num_states, _num_subtasks);
		_Nsas.resize(_num_bindings * _num_states, _num_subtasks);
		return true;
	}
	return false;
}


void HH_CompositeTask::initialize ()
{	HH_Task::initialize();
	_Nsa = 0;   // Initializing the N(s,a) tables

	// Initializing the N(s,a,s') table
	for (unsigned s = 0; s < _num_bindings * _num_states; s++)
		for (unsigned t = 0; t < _num_subtasks; t++)
			_Nsas(s,t).clear();
}


// Calculate E[Time(s')]
double HH_CompositeTask::expected_time (const int& s, const int& t) const
{	double E_T = 0.0;
	unordered_map<int,int> Nsas = _Nsas(s,t);
	for (const auto& ns : Nsas)
		E_T += (double(ns.second) / _Nsa(s,t)) * _Time[ns.first];
	return E_T;
}


// Calculate E[H(s')]
double HH_CompositeTask::expected_h (const int& s, const int& t) const
{	double E_H = 0.0;
	unordered_map<int,int> Nsas = _Nsas(s,t);
	for (const auto& ns : Nsas)
		E_H += (double(ns.second) / _Nsa(s,t)) * _H[ns.first];
	return E_H;
}


unsigned HH_CompositeTask::greedy_policy (const unsigned& agent, const vector<int>& parameters, const State& state)
{	vector<unsigned> adm_tasks = admissible_subtasks(agent, parameters, state);
	if (adm_tasks.size() == 1)
		return adm_tasks[0];
	else
	{	int s = hash(agent, parameters, state);
		double rho = (*_avg_time)[agent] > 0.0 ? (*_avg_reward)[agent]/(*_avg_time)[agent] : 0.0;
		double max_val = -numeric_limits<double>::max(), eqn;
		vector<int> gtasks;

		for (auto t = adm_tasks.cbegin(); t != adm_tasks.cend(); ++t)
		{	const int subtask_hash = _subtask[*t].link->hash(agent, subtask_bindings(agent, parameters, state, *t), state);
			eqn = (dynamic_cast<HH_Task*>(_subtask[*t].link)->h(subtask_hash) - rho * dynamic_cast<HH_Task*>(_subtask[*t].link)->time(subtask_hash))   // Average-adjusted reward for the child
						+ (expected_h(s, *t) - rho * expected_time(s, *t));   // Average-adjusted reward for completing the parent task
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


inline double HH_CompositeTask::time_update (const unsigned& agent, const vector<int>& parameters, const State& state)
{	int best_subtask = greedy_policy(agent, parameters, state);
	int s = hash(agent, parameters, state);
	return dynamic_cast<HH_Task*>(_subtask[best_subtask].link)->time(_subtask[best_subtask].link->hash(agent, subtask_bindings(agent, parameters, state, best_subtask), state))
							+ expected_time(s, best_subtask);
}


inline double HH_CompositeTask::total_reward_update (const unsigned& agent, const vector<int>& parameters, const State& state)
{	int best_subtask = greedy_policy(agent, parameters, state);
	int s = hash(agent, parameters, state);
	return dynamic_cast<HH_Task*>(_subtask[best_subtask].link)->h(_subtask[best_subtask].link->hash(agent, subtask_bindings(agent, parameters, state, best_subtask), state))
							+ expected_h(s, best_subtask);
}


void HH_CompositeTask::update (const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t, const State& next_state)
{	int s = hash(agent, parameters, state);

	// P(s'|s,a) update
	_Nsas(s,t)[hash(agent, parameters, next_state)]++;   // N(s,a,s') update
	_Nsa(s,t)++;   // N(s,a) update

	_Time[s] = time_update(agent, parameters, state);
	_H[s] = total_reward_update(agent, parameters, state);
}


string HH_CompositeTask::print (const State& state) const
{	ostringstream out;
	for (unsigned b = 0; b < _num_bindings; b++)
	{	out << "\n\n******** " << _name;
		auto parameters_and_state = unhash(0, b * _num_states, state);
		if (!parameters_and_state.first.empty())
			out << parameters_and_state.first;
		out << " ********\n";

		out << "\nH(s):\n";
		for (unsigned s = b * _num_states; s < (b + 1) * _num_states; s++)
			if (_H[s] != 0.0)
				out << "[" << unhash(0, s, state).second->print() << "]   " << _H[s] << endl;

		out << "\nTime(s):\n";
		for (unsigned s = b * _num_states; s < (b + 1) * _num_states; s++)
			if (_Time[s] != 0.0)
				out << "[" << unhash(0, s, state).second->print() << "]   " << _Time[s] << endl;

		out << "\nP(s'|s,a):\n";
		for (unsigned s = b * _num_states; s < (b + 1) * _num_states; s++)
		{	parameters_and_state = unhash(0, s, state);
			for (unsigned t = 0; t < _num_subtasks; t++)
			{	unordered_map<int,int> Nsas = _Nsas(s,t);
				for (const auto& ns : Nsas)
				{	out << "[" << parameters_and_state.second->print() << "], " << t << ", [";
					out << unhash(0, ns.first, state).second->print() << "]   " << double(ns.second) / _Nsa(s,t) << endl;
				}
			}
		}
	}
	return out.str();
}
