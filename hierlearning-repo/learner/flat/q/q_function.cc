
/**********************************************************************************

	Q FUNCTION
		Neville Mehta

***********************************************************************************/


#include <limits>
#include <sstream>
#include "../../../lib/common.h"
#include "q_function.h"


Q_function::Q_function (const string& state_variables, const unsigned& num_actions, const MDP& mdp) : Function(state_variables, num_actions, mdp)
{	resize(_num_states, num_actions);
}


double Q_function::max_Q (const int& s) const
{	double max_val = -numeric_limits<double>::max();
	for (unsigned a = 0; a < _num_actions; ++a)
		if (max_val < (*this)(s,a))
			max_val = (*this)(s,a);
	return max_val;
}


unsigned Q_function::greedy_policy (const unsigned& agent, const State& state)
{	int s = hash(agent, state);
	double max_val = -numeric_limits<double>::max();
	unsigned gtask = 0;
	unsigned ties = 0;

	for (unsigned a = 0; a < _num_actions; ++a)
	{	if (max_val < (*this)(s,a))
		{	max_val = (*this)(s,a);
			gtask = a;
			ties = 1;
		}
		else if (max_val == (*this)(s,a) && rand_real() < 1.0/++ties)
			gtask = a;
	}

	return gtask;
}


void Q_function::update (const unsigned& agent, const State& state, const unsigned& action, const double& reward, const double& duration, const State& next_state)
{	int s = hash(agent, state);
	(*this)(s, action) += q_function_parameters::ALPHA * (reward + pow(q_function_parameters::GAMMA, duration) * max_Q(hash(agent, next_state)) - (*this)(s, action));
}


string Q_function::print (const State& state) const
{	ostringstream out;
	out << "Alpha: " << q_function_parameters::ALPHA << endl;
	out << "\nQ(s,a):\n";
	for (unsigned s = 0; s < _num_states; ++s)
	{	unsigned a;
		for (a = 0; a < _num_actions; ++a)
			if ((*this)(s,a) != 0.0)
				break;

		if (a < _num_actions)
		{	out << "[" << unhash(0, s, state)->print() << "]";
			for (a = 0; a < _num_actions; ++a)
				out << "   " << (*this)(s,a);
			out << endl;
		}
	}
	return out.str();
}
