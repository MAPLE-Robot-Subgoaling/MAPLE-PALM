
/***********************************************************************

   	HIERARCHICAL H COMPOSITE TASK
		Neville Mehta

************************************************************************/


#pragma once


#include <string>
#include <unordered_map>
#include <valarray>
#include "../../../lib/matrix.h"
#include "../generic/composite_task.h"
#include "hh_task.h"



class HH_CompositeTask : public CompositeTask, public HH_Task
{	protected:
		matrix<int> _Nsa;   // N(s,a) table
		matrix<unordered_map<int,int>> _Nsas;   // N(s,a,s') table
		static valarray<double>* _avg_reward;
		static valarray<double>* _avg_time;

		double expected_time(const int& s, const int& t) const;   // P(s'|s,a).time(s') summation
		double expected_h(const int& s, const int& t) const;   // P(s'|s,a).h(s') summation
		double time_update(const unsigned& agent, const vector<int>& parameters, const State& state);
		double total_reward_update(const unsigned& agent, const vector<int>& parameters, const State& state);

	public:
		HH_CompositeTask(const string& task_name, const string& parameters, const string& state_variables,
					const vector<Subtask>& subtasks, bool (*function)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp);
		static void set_rho (valarray<double>* avg_reward, valarray<double>* avg_time) { _avg_reward = avg_reward; _avg_time = avg_time; }
		void add_subtask(const Subtask& subtask, const MDP& mdp);
		bool update_parameters(const vector<int>& parameter_size);
		virtual void initialize();
		unsigned greedy_policy(const unsigned& agent, const vector<int>& parameters, const State& state);
		virtual void update(const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t, const State& next_state);
		string print(const State& state) const;
		virtual ~HH_CompositeTask() {}
};
