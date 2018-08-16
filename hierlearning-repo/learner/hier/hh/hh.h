
/*************************************************

	HIERARCHICAL H LEARNER
		Neville Mehta

**************************************************/


#pragma once


#include <valarray>
#include "../generic/hier_learner.h"


class HH : public HierLearner
{	valarray<double> avg_reward, avg_time;

	public:
		HH(const string& learner_name, const MDP& mdp, const unique_ptr<Hierarchy>& hierarchy, const unsigned& run_index);
		void initialize(const MDP& mdp);
		void update(const State& next_state, const valarray<double>& reward, const valarray<double>& elapsed_time);
		~HH () {}
};
