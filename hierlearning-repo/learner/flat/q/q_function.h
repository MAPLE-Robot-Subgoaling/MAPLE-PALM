
/**********************************************************************************

	Q FUNCTION HEADER
		Neville Mehta

***********************************************************************************/


#pragma once

#include "../../../lib/matrix.h"
#include "../generic/function.h"


namespace q_function_parameters {
const double ALPHA = 0.1;   // Learning rate
const double GAMMA = 1.0;   // Discount factor
}


class Q_function : public Function, public matrix<double>
{	double max_Q(const int& s) const;   // max_a Q(s,a)

	public:
		Q_function(const string& state_variables, const unsigned& num_actions, const MDP& mdp);
		void initialize () { *_data = 0.0; }
		unsigned greedy_policy(const unsigned& agent, const State& state);
		void update(const unsigned& agent, const State& state, const unsigned& action, const double& reward, const double& duration, const State& next_state);
		string print(const State& state) const;
		~Q_function () {}
};
