
/*************************************************

	HIERARCHICAL LEARNER
		Neville Mehta

**************************************************/


#pragma once


#include <list>
#include <vector>
#include "../../../domain/mdp.h"
#include "../../../hiergen/hierarchy.h"
#include "task.h"
#include "../../learner.h"


class HierLearner : public Learner
{	private:
		// Task stack for controlled recursion
		struct Task_stack
		{	Task* task;   // Pointer to task module
			vector<int> parameters;   // Parameter bindings for the task
			unique_ptr<State> state;   // World state when the subtask is invoked
			unsigned subtask;   // Index of subtask
			double time;   // Time to execute the task (for discounted learners)

			Task_stack (Task* const task, const vector<int>& parameters, const double& time = 0.0) : task(task), parameters(parameters), time(time) {}
			~Task_stack () {}
		};

	protected:
		Task* Root_task;
		list<Task*> Task_list;   // List of all tasks
		vector<list<Task_stack>> task_stack; // Front of the list = top of stack (Can't use STL stack because we need to check the entire stack for task termination)

		void create_task_list();

	public:
		HierLearner(const string& learner_name, const MDP& mdp);
		template <typename CTask, typename PTask, typename RTask> void flat_hierarchy_designer(const MDP& mdp);   // Composite task, Primitive task, Root task
		template <typename CTask, typename PTask, typename RTask> void task_hierarchy_designer(const string& type, const MDP& mdp);
		virtual void initialize(const MDP& mdp);
		virtual void reset();
		virtual vector<int> greedy_policy(const State& state);
		virtual vector<int> exploratory_policy(const State& state);
		virtual vector<int> learned_policy(const State& state);
		virtual void print_hierarchy(const string& filename) const;
		virtual void write_text_file(const string& filename, const MDP& mdp) const;
		virtual ~HierLearner();
};

#include "hier_learner.inl"
