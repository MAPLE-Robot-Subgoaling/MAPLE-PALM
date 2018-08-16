
/***********************************************************************

   	HIERARCHICAL H ROOT TASK
		Neville Mehta

************************************************************************/


#include <limits>
#include <string>
#include <vector>
#include "hh_root_task.h"


HH_RootTask::HH_RootTask (const string& task_name, const string& parameters, const string& state_variables,
			const vector<Subtask>& subtasks, bool (*func)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp)
							: Task(task_name, parameters, state_variables, mdp), HH_CompositeTask(task_name, parameters, state_variables, subtasks, func, expression, mdp)
{	_greedy_subtask.resize(mdp.state().num_agents());
	_alpha.resize(mdp.state().num_agents());
}


void HH_RootTask::initialize ()
{	HH_CompositeTask::initialize();
	_alpha = 1.0;
}


double HH_RootTask::avg_adjusted_update (const unsigned& agent, const vector<int>& parameters, const State& state)
{	vector<unsigned> adm_tasks = admissible_subtasks(agent, parameters, state);
	int s = hash(agent, parameters, state);
	double rho = (*_avg_time)[agent] > 0.0 ? (*_avg_reward)[agent]/(*_avg_time)[agent] : 0.0;
	double max_val = -numeric_limits<double>::max(), eqn;

	for (auto t = adm_tasks.cbegin(); t != adm_tasks.cend(); ++t)
	{	const int subtask_hash = _subtask[*t].link->hash(agent, subtask_bindings(agent, parameters, state, *t), state);
		eqn = dynamic_cast<HH_Task*>(_subtask[*t].link)->h(subtask_hash) - rho * dynamic_cast<HH_Task*>(_subtask[*t].link)->time(subtask_hash) + expected_h(s, *t);
		if (max_val < eqn)
			max_val = eqn;
	}

	return max_val;
}


unsigned HH_RootTask::greedy_policy (const unsigned& agent, const vector<int>& parameters, const State& state)
{	_greedy_subtask[agent] = true;
	return HH_CompositeTask::greedy_policy(agent, parameters, state);
}


unsigned HH_RootTask::exploratory_policy (const unsigned& agent, const vector<int>& parameters, const State& state)
{	vector<unsigned> adm_tasks = admissible_subtasks(agent, parameters, state);
	unsigned num_admissible_tasks = adm_tasks.size();
	if (num_admissible_tasks == 1)
	{	_greedy_subtask[agent] = true;
		return adm_tasks[0];
	}

	if (rand_real() < composite_task_parameters::EPSILON)   // Random
	{	_greedy_subtask[agent] = false;
		return adm_tasks[rand_int(num_admissible_tasks)];
	}
	else
		return greedy_policy(agent, parameters, state);
}


void HH_RootTask::update (const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t, const State& next_state)
{	int s = hash(agent, parameters, state);

	// P(s'|s,a) update
	_Nsas(s,t)[hash(agent, parameters, next_state)]++;   // N(s,a,s') update
	_Nsa(s,t)++;   // N(s,a) update

	if (_greedy_subtask[agent])   // Root's subtask has been greedily selected
	{	int subtask_hash = _subtask[t].link->hash(agent, subtask_bindings(agent, parameters, state, t), state);
		(*_avg_reward)[agent] += _alpha[agent] * (dynamic_cast<HH_Task*>(_subtask[t].link)->h(subtask_hash)	+ _H[hash(agent, parameters, next_state)] - _H[s] - (*_avg_reward)[agent]);
		(*_avg_time)[agent] += _alpha[agent] * (dynamic_cast<HH_Task*>(_subtask[t].link)->time(subtask_hash) - (*_avg_time)[agent]);
		_alpha[agent] /= (_alpha[agent] + 1);
	}

	_H[s] = avg_adjusted_update(agent, parameters, state);
}
