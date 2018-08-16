
/**********************************************************************************

	MAXQ COMPOSITE TASK
		Neville Mehta

***********************************************************************************/


#pragma once


#include <string>
#include <vector>
#include "../../../lib/matrix.h"
#include "../../../hiergen/expression.h"
#include "../generic/composite_task.h"
#include "maxq_task.h"


namespace maxq_composite_task {
const double GAMMA = 1.0;   // Discount factor
const double ALPHA = 0.1;
}


class MaxQ_CompositeTask : public CompositeTask, public MaxQ_Task
{	protected:
		matrix<double> _C;   // Completion function

	public:
		MaxQ_CompositeTask(const string& task_name, const string& parameters, const string& state_variables,
					const vector<Subtask>& subtasks, bool (*function)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp);
		void add_subtask(const Subtask& subtask, const MDP& mdp);
		bool update_parameters(const vector<int>& parameter_size);
		void initialize();
		double V(const unsigned& agent, const vector<int>& parameters, const State& state) const;
		unsigned greedy_policy(const unsigned& agent, const vector<int>& parameters, const State& state);
		void update(const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t, const State& next_state, const double& N);
		string print(const State& state) const;
		~MaxQ_CompositeTask() {}
};
