
/**********************************************************************************

	SEQUENCED TASK
		Neville Mehta

***********************************************************************************/


#pragma once


#include <string>
#include <vector>
#include "composite_task.h"



class SequencedTask : public CompositeTask
{	unsigned _subtask_index;

	unsigned sequenced_subtask(const unsigned& agent, const vector<int>& parameters, const State& state) const;

	public:
		SequencedTask(const string& task_name, const string& parameters, const string& state_variables, const vector<Subtask>& subtasks,
																bool (*function)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp)
				: Task(task_name, parameters, state_variables, mdp), CompositeTask(task_name, parameters, state_variables, subtasks, function, expression, mdp), _subtask_index(0) {}
		unsigned greedy_policy (const unsigned& agent, const vector<int>& parameters, const State& state) const { return sequenced_subtask(agent, parameters, state); }
		unsigned exploratory_policy (const unsigned& agent, const vector<int>& parameters, const State& state) const { return sequenced_subtask(agent, parameters, state); }
		void update (const int& subtask_index) { _subtask_index = subtask_index + 1; }
		bool termination (const unsigned& agent, const vector<int>& parameters, const State& state) const { return _subtask_index >= _num_subtasks; }
		~SequencedTask () {}
};
