
/*************************************************

	GENERIC LEARNER
		Neville Mehta

**************************************************/


#pragma once

#include "../domain/mdp.h"


class Learner
{	protected:
		string _name;

	public:
		Learner (const string& learner_name) : _name(learner_name) {}
		string name () const { return _name; }
		virtual void initialize(const MDP& mdp) = 0;
		virtual void reset() = 0;
		virtual vector<int> greedy_policy(const State& state) = 0;
		virtual vector<int> exploratory_policy(const State& state) = 0;
		virtual vector<int> learned_policy(const State& state) = 0;
		virtual void update(const State& next_state, const valarray<double>& reward, const valarray<double>& duration) = 0;
		virtual void write_text_file(const string& filename, const MDP& mdp) const = 0;
		virtual ~Learner () {}
};
