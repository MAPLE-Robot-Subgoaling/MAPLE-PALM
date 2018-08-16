
/*************************************************

	MAXQ LEARNER
		Neville Mehta

**************************************************/


#pragma once


#include "../generic/hier_learner.h"


class MaxQ : public HierLearner
{	public:
		MaxQ(const string& learner_name, const MDP& mdp, const unique_ptr<Hierarchy>& hierarchy, const unsigned& run_index);
		void update(const State& next_state, const valarray<double>& reward, const valarray<double>& duration);
		~MaxQ() {}
};
