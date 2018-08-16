
/*************************************************

	MODEL INTERFACE
		Neville Mehta

**************************************************/


#pragma once

#include <map>
#include <memory>
#include <vector>
#include "../domain/mdp.h"
#include "expression.h"



class ModelTree
{	vector<pair<Expression, ModelTree>> choices;
	int leaf;

	ModelTree(list<string>& model_file_lines, const State& state, const int& level = 0);
	void read_file(list<string>& model_file_lines, const State& state, const int& level);   // Weka v3.6.5 J48 tree format
	bool is_leaf () const { return choices.empty(); }
	inline int true_choice(const State& state) const;
	inline int true_choice(const map<int,int>& variable_value) const;
	string print_gv_cluster(const string& node_prefix, int& id) const;

	public:
		ModelTree(const int& leaf = 0) : leaf(leaf) {}
		ModelTree(const string& filename, const State& state);
		int get_value(const State& state) const;
		int get_value(const map<int,int>& variable_value) const;
		set<int> leaf_values() const;
		bool is_persistent(const int& variable) const;
		set<int> checked_variables() const;
		set<int> context_checked_variables(const map<int,int>& variable_value) const;
		string print_gv_cluster (const string& node_prefix) const { int id = 0; return print_gv_cluster(node_prefix, id); }
		~ModelTree () {}
};


class ActionModel
{	struct SingleActionModel
	{	map<int, ModelTree> transition;
		set<int> transition_checked_variables;
		ModelTree reward;
		set<int> reward_checked_variables;
		set<int> reward_checked_variables_depth_2;
	};
	map<int, SingleActionModel> _model;

	void read(const string& directory, const MDP& mdp);
	void compute_checked_variables();
	set<int> closure_next_stage(const pair<int, SingleActionModel>& am, const set<int>& closure_variables) const;
	void print_context_precondition(const MDP& mdp) const;

	public:
		ActionModel(const string& trajectory_filename, const string& model_directory, const MDP& mdp);
		ActionModel(const vector<vector<unique_ptr<State_Action_Reward>>>& input_trajectories, const string& model_directory, const unsigned& run_index, const MDP& mdp);
		set<int> checked_variables(const int& action) const;
		set<int> reward_variables(const int& action) const;
		set<int> context_checked_variables(const int& action, const map<int,int>& variable_value) const;
		set<int> context_changed_variables(const int& action, const map<int,int>& variable_value) const;
		set<int> closure(const set<int>& actions, const set<int>& variables) const;
		void print_gv(const string& directory, const MDP& mdp) const;
};
