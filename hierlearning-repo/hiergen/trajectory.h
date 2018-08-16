
/*************************************************

	CAUSALLY ANNOTATED TRAJECTORY
		Neville Mehta

**************************************************/


#pragma once


#include <map>
#include <memory>
#include <set>
#include <string>
#include <vector>
#include "../domain/mdp.h"
#include "model.h"


struct Subtrajectory
{	set<int> action_indices;   // Action indices of the actions within a trajectory
	map<int,int> preconditions;   // variable -> action index
	set<int> outgoing_variables;

	Subtrajectory (const set<int>& outgoing_variables = set<int>()) : outgoing_variables(outgoing_variables) {}
	bool empty () const { return action_indices.empty(); }
	void clear () { action_indices.clear(); }
};


class Trajectory
{	enum Special_Actions {Start_Action = -2, End_Action};

	struct Trj_Node
	{	map<int,int> state;   // State in the trajectory before the action is executed
		int action;
		double reward;

		struct Trj_Arc
		{	map<int,int> incoming;   // Variable id, temporal index from trajectory node
			map<int,set<int>> outgoing;   // Variable id, temporal indices of trajectory nodes

			Trj_Arc() {}
		};
		Trj_Arc causal_arcs;   // Multiple arcs; each arcs has multiple labels

		Trj_Node (const map<int,int>& state, const int& action, const double& reward = 0.0) : state(state), action(action), reward(reward) {}
	};
	vector<Trj_Node> _trajectory;

	set<int> incoming_variables(const set<int>& action_indices) const;
	set<int> outgoing_variables(const set<int>& action_indices) const;

	public:
		Trajectory(istream& in, const MDP& mdp, const ActionModel& action_model);
		Trajectory(const vector<unique_ptr<State_Action_Reward>>& sample_trajectory, const ActionModel& action_model);
		Trajectory(const Trajectory& trajectory, const set<int>& action_indices);
		unsigned size () const { return _trajectory.size(); }
		void causal_annotation(const ActionModel& action_model, const set<int>& goal_variables, const set<int>& transfer_variables);
		void relevance_annotation(const ActionModel& action_model);
		void cleanup();
		map<int,int> extract_goal_state () const { return _trajectory.back().state; }
		map<int,int> extract_state (const int& action_index) const { return _trajectory[action_index].state; }
		set<int> extract_actions() const;
		int extract_action (const int& action_index) const { return _trajectory[action_index].action; }
		set<int> extract_actions (const set<int>& action_indices) const;
		set<int> checked_variables() const;
		set<int> changed_variables() const;
		set<int> incoming_variables (const int& action_index) const { return incoming_variables(make_set<int>(1, action_index)); }
		set<int> outgoing_variables (const int& action_index) const { return outgoing_variables(make_set<int>(1, action_index)); }
		set<int> goal_variables () const { return incoming_variables(_trajectory.size() - 1); }
		int causal_action_index(const int& action_index, const int& variable) const;
		unique_ptr<Subtrajectory> goal() const;
		unique_ptr<Subtrajectory> extract_subtrajectory(const set<int>& action_indices) const;
		unique_ptr<Subtrajectory> extract_subtrajectory(const int& action_index) const { return extract_subtrajectory(make_set<int>(1, action_index)); }
		unique_ptr<Subtrajectory> splitter() const;
		bool always_true(const Expression& exp) const;
		map<map<int,int>,double> cumulative_reward(const set<int>& action_indices) const;
		void print_gv(const string& filename, const MDP& mdp) const;
		~Trajectory () {}
};
