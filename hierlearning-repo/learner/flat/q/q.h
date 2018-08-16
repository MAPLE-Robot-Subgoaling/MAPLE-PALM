
/*************************************************

	Q LEARNER
		Neville Mehta

**************************************************/


#pragma once

#include "../generic/flat_learner.h"
#include "q_function.h"


class Q : public FlatLearner
{	public:
		Q (const string& learner_name, const MDP& mdp) : FlatLearner(learner_name, unique_ptr<Function>(new Q_function(mdp_state_variables(mdp), mdp.actions().size(), mdp)), mdp) {}
		~Q () {}
};
