
/**********************************************************************************

	H FUNCTION
		Neville Mehta

***********************************************************************************/


#include <limits>
#include <sstream>
#include "../../../lib/common.h"
#include "h_function.h"


H_function::H_function (const string& state_variables, const unsigned& num_actions, const MDP& mdp) : Function(state_variables, num_actions, mdp)
{	_H.resize(_num_states);
	_Reward.resize(_num_states, _num_actions);
	_Time.resize(_num_states, _num_actions);
	_Nsa.resize(_num_states, _num_actions);
	_Nsas.resize(_num_states, _num_actions);
	_avg_reward.resize(mdp.state().num_agents());
	_avg_time.resize(mdp.state().num_agents());
	_alpha.resize(mdp.state().num_agents());
	_greedy_action_selected.resize(mdp.state().num_agents());
}


void H_function::initialize ()
{	_H = 0.0;
	_Reward = 0.0;
	_Time = 0.0;
	_Nsa = 0;

	// Initializing the N(s,a,s') table
	for (unsigned s = 0; s < _num_states; s++)
		for (unsigned a = 0; a < _num_actions; a++)
			_Nsas(s,a).clear();

	_avg_reward = 0.0;
	_avg_time = 0.0;
	_alpha = 1.0;
}


// Calculate E[H(s')]
double H_function::expected_H (const int& s, const int& a) const
{	double E_H = 0.0;
	unordered_map<int,int> Nsas = _Nsas(s,a);
	for (const auto& ns : Nsas)
		E_H += (double(ns.second) / _Nsa(s,a)) * _H[ns.first];
	return E_H;
}


double H_function::max_H (const unsigned& agent, const State& state) const
{	int s = hash(agent, state);
	double max_val = -numeric_limits<double>::max();
	for (unsigned a = 0; a < _num_actions; ++a)
	{	double eqn = _Reward(s,a) - rho(agent) * _Time(s,a) + expected_H(s,a);
		if (max_val < eqn)
			max_val = eqn;
	}
	return max_val;
}


unsigned H_function::greedy_policy (const unsigned& agent, const State& state)
{	int s = hash(agent, state);
	_greedy_action_selected[agent] = true;
	double max_val = -numeric_limits<double>::max();
	unsigned gtask = 0;
	unsigned ties = 0;

	for (unsigned a = 0; a < _num_actions; ++a)
	{	double eqn = _Reward(s,a) - rho(agent) * _Time(s,a) + expected_H(s,a);
		if (max_val < eqn)
		{	max_val = eqn;
			gtask = a;
			ties = 1;
		}
		else if (max_val == eqn && rand_real() < 1.0/++ties)
			gtask = a;
	}

	return gtask;
}


unsigned H_function::exploratory_policy (const unsigned& agent, const State& state)
{	if (rand_real() < function_parameters::EPSILON)
	{	_greedy_action_selected[agent] = false;
		return rand_int(_num_actions);
	}
	else
	{	_greedy_action_selected[agent] = true;
		return greedy_policy(agent, state);
	}
}


void H_function::update (const unsigned& agent, const State& state, const unsigned& action, const double& reward, const double& duration, const State& next_state)
{	int s = hash(agent, state);

	// P(s'|s,a) update
	_Nsas(s, action)[hash(agent, next_state)]++;
	_Nsa(s, action)++;
	
	_Time(s, action) += (duration - _Time(s, action)) / _Nsa(s, action);
	_Reward(s, action) += (reward - _Reward(s, action)) / _Nsa(s, action);

	// rho update
	if (_greedy_action_selected[agent])
	{	_avg_reward[agent] += _alpha[agent] * (_Reward(s, action) + _H[hash(agent, next_state)] - _H[s] - _avg_reward[agent]);
		_avg_time[agent] += _alpha[agent] * (_Time(s, action) - _avg_time[agent]);
		_alpha[agent] /= (_alpha[agent] + 1);
	}

	_H[s] = max_H(agent, state);
}


string H_function::print (const State& state) const
{	ostringstream out;

	out << "\nRho = " << valarray<double>(_avg_reward / _avg_time);
	out << "\nAverage reward = " << _avg_reward;
	out << "\nAverage time = " << _avg_time << endl;

	out << "\nH(s):\n";
	for (unsigned s = 0; s < _num_states; ++s)
		if (_H[s] != 0.0)
			out << "[" << unhash(0, s, state)->print() << "]   " << _H[s] << endl;

	out << "\nReward(s,a):\n";
	for (unsigned s = 0; s < _num_states; ++s)
	{	unsigned a;
		for (a = 0; a < _num_actions; ++a)
			if (_Reward(s,a) != 0.0)
				break;

		if (a < _num_actions)
		{	out << "[" << unhash(0, s, state)->print() << "]";
			for (a = 0; a < _num_actions; ++a)
				out << "   " << _Reward(s,a);
			out << endl;
		}
	}

	out << "\nTime(s,a):\n";
	for (unsigned s = 0; s < _num_states; ++s)
	{	unsigned a;
		for (a = 0; a < _num_actions; ++a)
			if (_Time(s,a) != 0.0)
				break;

		if (a < _num_actions)
		{	out << "[" << unhash(0, s, state)->print() << "]";
			for (a = 0; a < _num_actions; ++a)
				out << "   " << _Time(s,a);
			out << endl;
		}
	}

	out << "\nP(s'|s,a):\n";
	for (unsigned s = 0; s < _num_states; ++s)
	{	unique_ptr<State> _state = unhash(0, s, state);
		for (unsigned a = 0; a < _num_actions; a++)
		{	unordered_map<int,int> Nsas = _Nsas(s,a);
			for (const auto& ns : Nsas)
			{	out << "[" << _state->print() << "], " << a << ", [";
				out << unhash(0, ns.first, state)->print() << "]   " << double(ns.second) / _Nsa(s,a) << endl;
			}
		}
	}

	return out.str();
}
