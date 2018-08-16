
/***********************************************************************

   	HIERARCHICAL H ROOT TASK
		Neville Mehta

************************************************************************/


#pragma once


#include <string>
#include <vector>
#include "hh_composite_task.h"



class HH_RootTask : public HH_CompositeTask
{	private:
		valarray<bool> _greedy_subtask;
		valarray<double> _alpha;

		double avg_adjusted_update(const unsigned& agent, const vector<int>& parameters, const State& state);

	public:
		HH_RootTask (const string& task_name, const string& parameters, const string& state_variables,
					const vector<Subtask>& subtasks, bool (*func)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp);
		void initialize();
		unsigned greedy_policy(const unsigned& agent, const vector<int>& parameters, const State& state);
		unsigned exploratory_policy(const unsigned& agent, const vector<int>& parameters, const State& state);
		void update(const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t, const State& next_state);
		string print (const State& state) const { return "\nAverage Reward = " + to_string(*_avg_reward) + "\nAverage Time = " + to_string(*_avg_time) + "\n" + HH_CompositeTask::print(state); }
		~HH_RootTask () {}
};
