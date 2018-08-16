
/*************************************************

	FLAT LEARNER
		Neville Mehta

**************************************************/


#pragma once


#include <memory>
#include <string>
#include <vector>
#include "../../../domain/mdp.h"
#include "../../learner.h"
#include "function.h"


class FlatLearner : public Learner
{	const vector<int> action_encoding;   // Action encodings
	unique_ptr<State> current_state;   // Current states of the agents
	vector<unsigned> current_action;   // Current actions of the agents
	unique_ptr<Function> function;

	protected:
		string mdp_state_variables(const MDP& mdp) const;

	public:
		FlatLearner (const string& learner_name, unique_ptr<Function> function, const MDP& mdp) : Learner(learner_name), action_encoding(mdp.actions()), current_action(vector<unsigned>(mdp.state().num_agents(), 0)), function(move(function)) {}
		void initialize(const MDP& mdp);
		void reset () {}
		vector<int> greedy_policy(const State& state);
		vector<int> exploratory_policy(const State& state);
		vector<int> learned_policy (const State& state) { return greedy_policy(state); }
		void update(const State& next_state, const valarray<double>& reward, const valarray<double>& duration);
		void write_text_file(const string& filename, const MDP& mdp) const;
		virtual ~FlatLearner () {}
};
