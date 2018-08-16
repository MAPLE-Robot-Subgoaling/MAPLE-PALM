
/*************************************************

	HIERARCHICAL ANALYSIS
		Neville Mehta

**************************************************/


#pragma once


#include <memory>
#include "trajectory.h"
#include "catscan.h"
#include "../learner/hier/generic/composite_task.h"


class Hierarchy
{	bool _is_root;
	int _primitive_action;
	set<int> _abstraction;
	Expression _admissibility;
	Expression _termination;
	vector<Hierarchy*> _subtasks;
	vector<pair<unsigned,Hierarchy*>> _subtask_library;

	Hierarchy(const set<int>& abstraction, const Expression& admissibility, const Expression& termination, const vector<Trajectory>& trajectories, const ActionModel& action_model)
				: _is_root(false), _primitive_action(0), _abstraction(abstraction), _admissibility(admissibility), _termination(termination) { hierarchicalize(trajectories, action_model); }
	Hierarchy(const set<int>& abstraction, const Expression& admissibility, const Expression& termination, const vector<Hierarchy*>& subtasks)
						: _is_root(false), _primitive_action(0), _abstraction(abstraction), _admissibility(admissibility), _termination(termination), _subtasks(subtasks) {}
	Hierarchy (const int& primitive_action, const set<int>& abstraction) : _is_root(false), _primitive_action(primitive_action), _abstraction(abstraction) {}
	void hierarchicalize(const vector<Trajectory>& trajectories, const ActionModel& action_model);
	void termination_condition(const vector<Trajectory>& trajectories);
	void state_abstraction(const vector<Trajectory>& trajectories, const ActionModel& action_model, const set<int>& goal_variables);
	vector<Hierarchy*> incorporate_subtasks(const CatScan& catscan, const ActionModel& action_model, const unsigned& index = 0, const bool& precondition_ordering = true, const bool& admissibility_wrapper = false);
	map<int,set<int>> extract_action_abstractions(const vector<Trajectory>& trajectories, const ActionModel& action_model) const;
	template <typename CTask, typename PTask, typename RTask> CompositeTask::Subtask task_hierarchy_builder(const MDP& mdp, list<Task*>& Task_list, map<Hierarchy*,CompositeTask::Subtask>& memoizer) const;
	string abstraction_string(const set<int>& abstraction, const MDP& mdp) const;
	bool combinable_tasks(vector<CompositeTask::Subtask>& subtasks, Task* target_task) const;
	bool connected_tasks(Task* source_task, Task* target_task) const;
	void print_cats(const string& directory, const vector<Trajectory>& trajectories, const MDP& mdp) const;

	public:
		Hierarchy(const string& trajectory_filename, const string& model_directory, const MDP& mdp);
		Hierarchy(const vector<vector<unique_ptr<State_Action_Reward>>>& input_trajectories, const string& model_directory, const unsigned& run_index, const MDP& mdp);
		template <typename CTask, typename PTask, typename RTask> void task_hierarchy_builder(const MDP& mdp, list<Task*>& Task_list) const;
		~Hierarchy();
};

#include "hierarchy.inl"
