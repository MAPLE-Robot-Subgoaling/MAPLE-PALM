
/**********************************************************************************

	H FUNCTION HEADER
		Neville Mehta

***********************************************************************************/


#pragma once

#include <unordered_map>
#include <valarray>
#include "../../../lib/matrix.h"
#include "../generic/function.h"


class H_function : public Function
{	valarray<double> _H;
	matrix<double> _Reward;
	matrix<double> _Time;
	matrix<int> _Nsa;
	matrix<unordered_map<int,int>> _Nsas;
	valarray<double> _avg_reward;
	valarray<double> _avg_time;
	valarray<double> _alpha;
	valarray<bool> _greedy_action_selected;
	
	double rho (const unsigned& agent) const { return _avg_time[agent] > 0.0 ? _avg_reward[agent] / _avg_time[agent] : 0.0; }
	double expected_H(const int& s, const int& a) const;
	double max_H(const unsigned& agent, const State& state) const;

	public:
		H_function(const string& state_variables, const unsigned& num_actions, const MDP& mdp);
		void initialize();
		unsigned greedy_policy(const unsigned& agent, const State& state);
		unsigned exploratory_policy(const unsigned& agent, const State& state);
		void update(const unsigned& agent, const State& state, const unsigned& action, const double& reward, const double& duration, const State& next_state);
		string print(const State& state) const;
		~H_function () {}
};
