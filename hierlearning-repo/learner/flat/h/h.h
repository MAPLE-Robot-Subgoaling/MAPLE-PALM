
/*************************************************

	H LEARNER
		Neville Mehta

**************************************************/


#pragma once

#include "../generic/flat_learner.h"
#include "h_function.h"


class H : public FlatLearner
{	public:
		H (const string& learner_name, const MDP& mdp) : FlatLearner(learner_name, unique_ptr<Function>(new H_function(mdp_state_variables(mdp), mdp.actions().size(), mdp)), mdp) {}
		~H () {}
};
