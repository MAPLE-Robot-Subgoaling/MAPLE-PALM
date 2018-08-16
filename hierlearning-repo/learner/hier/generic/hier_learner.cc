
/*****************************************************************************************

   	HIERARCHICAL LEARNER
		Neville Mehta

******************************************************************************************/


#include <algorithm>
#include <fstream>
#include <functional>
#include <sstream>
#include "../../../domain/domains.h"
#include "primitive_task.h"
#include "hier_learner.h"



HierLearner::HierLearner (const string& learner_name, const MDP& mdp) : Learner(learner_name)
{	task_stack.resize(mdp.state().num_agents());   // One task stack per agent
}


void HierLearner::create_task_list ()
{	list<Task*> open_tasks(1, Root_task), primitive_tasks;
	while (!open_tasks.empty())
	{	CompositeTask* composite_task = dynamic_cast<CompositeTask*>(open_tasks.front());
		for (unsigned t = 0; t < composite_task->num_subtasks(); ++t)
		{	Task* child_task = composite_task->get_subtask(t);
			if (find(Task_list.begin(), Task_list.end(), child_task) != Task_list.end()
					|| find(primitive_tasks.begin(), primitive_tasks.end(), child_task) != primitive_tasks.end()
						|| find(open_tasks.begin(), open_tasks.end(), child_task) != open_tasks.end())
				continue;

			if (child_task->primitive())
				primitive_tasks.push_back(child_task);
			else
				open_tasks.push_back(child_task);
		}
		Task_list.push_back(composite_task);
		open_tasks.pop_front();
	}
	Task_list.insert(Task_list.end(), primitive_tasks.begin(), primitive_tasks.end());
}


void HierLearner::initialize (const MDP& mdp)
{	// Initialize the agent's innards
	if (Task_list.empty())
		create_task_list();
	task_stack.resize(mdp.state().num_agents());   // One task stack per agent
	reset();
	for_each(Task_list.begin(), Task_list.end(), [](Task* task){ task->initialize(); });
}


void HierLearner::reset ()
{	// Empty the stack(s) of everything but the Root task(s)
	for (auto& agent_task_stack : task_stack)
	{	agent_task_stack.clear();
		agent_task_stack.emplace_front(Root_task, vector<int>());   // Push the Root task with no bindings
	}
}


vector<int> HierLearner::greedy_policy (const State& state)
{	vector<int> actions(state.num_agents(), -1);
	for (unsigned agent = 0; agent < state.num_agents(); ++agent)
		if (state.action_complete(agent) && !task_stack[agent].empty())
		{	// Finding the oldest ancester that has terminated; we start from the bottom (back) of the stack (list)
			auto task_itr = task_stack[agent].rbegin();
			for ( ; task_itr != task_stack[agent].rend() && !task_itr->task->terminated(agent, task_itr->parameters, state); ++task_itr);
			if (task_itr != task_stack[agent].rend())
				// Removing this oldest terminated ancestor and all of its children from the task stack
				task_stack[agent].erase(task_stack[agent].begin(), task_itr.base());
			if (task_stack[agent].empty())
				continue;

			while (!task_stack[agent].front().task->primitive())
			{	task_stack[agent].front().state = state.clone();   // Store current state info on task stack

				CompositeTask* composite_task = dynamic_cast<CompositeTask*>(task_stack[agent].front().task);
				if (!composite_task)
					throw HierException(__FILE__, __LINE__, "Composite task expected.");

				task_stack[agent].front().subtask = composite_task->greedy_policy(agent, task_stack[agent].front().parameters, state);

				// Push new subtask and initialize its running time to 0
				task_stack[agent].emplace_front(composite_task->get_subtask(task_stack[agent].front().subtask),
												composite_task->subtask_bindings(agent, task_stack[agent].front().parameters, state, task_stack[agent].front().subtask));
			}
			task_stack[agent].front().state = state.clone();
			actions[agent] = dynamic_cast<PrimTask*>(task_stack[agent].front().task)->primitive_action();
		}

	return actions;
}


vector<int> HierLearner::exploratory_policy (const State& state)
{	vector<int> actions(state.num_agents(), -1);
	for (unsigned agent = 0; agent < state.num_agents(); ++agent)
		if (state.action_complete(agent) && !task_stack[agent].empty())
		{	// Finding the oldest ancester that has terminated; we start from the bottom (back) of the stack (list)
			auto task_itr = task_stack[agent].rbegin();
			for ( ; task_itr != task_stack[agent].rend() && !task_itr->task->terminated(agent, task_itr->parameters, state); ++task_itr);
			if (task_itr != task_stack[agent].rend())
				// Removing this oldest terminated ancestor and all of its children from the task stack
				task_stack[agent].erase(task_stack[agent].begin(), task_itr.base());
			if (task_stack[agent].empty())
				continue;

			while (!task_stack[agent].front().task->primitive())
			{	task_stack[agent].front().state = state.clone();   // Store current state info on task stack

				CompositeTask* composite_task = dynamic_cast<CompositeTask*>(task_stack[agent].front().task);
				if (!composite_task)
					throw HierException(__FILE__, __LINE__, "Composite task expected.");

				task_stack[agent].front().subtask = composite_task->exploratory_policy(agent, task_stack[agent].front().parameters, state);

				// Push new subtask and initialize its running time to 0
				task_stack[agent].emplace_front(composite_task->get_subtask(task_stack[agent].front().subtask),
												composite_task->subtask_bindings(agent, task_stack[agent].front().parameters, state, task_stack[agent].front().subtask));
			}
			task_stack[agent].front().state = state.clone();
			actions[agent] = dynamic_cast<PrimTask*>(task_stack[agent].front().task)->primitive_action();
		}

	return actions;
}


vector<int> HierLearner::learned_policy (const State& state)
{	vector<int> actions(state.num_agents(), -1);
	for (unsigned agent = 0; agent < state.num_agents(); ++agent)
		if (state.action_complete(agent) && !task_stack[agent].empty())
		{	// Finding the oldest ancester that has terminated; we start from the bottom (back) of the stack (list)
			auto task_itr = task_stack[agent].rbegin();
			for ( ; task_itr != task_stack[agent].rend() && !task_itr->task->terminated(agent, task_itr->parameters, state); ++task_itr);
			if (task_itr != task_stack[agent].rend())
				// Removing this oldest terminated ancestor and all of its children from the task stack
				task_stack[agent].erase(task_stack[agent].begin(), task_itr.base());
			if (task_stack[agent].empty())
				continue;

			while (!task_stack[agent].front().task->primitive())
			{	CompositeTask* composite_task = dynamic_cast<CompositeTask*>(task_stack[agent].front().task);
				if (!composite_task)
					throw HierException(__FILE__, __LINE__, "Composite task expected.");

				// Selecting the subtask greedily
				task_stack[agent].front().subtask = composite_task->greedy_policy(agent, task_stack[agent].front().parameters, state);

				// Placing the new subtask at top-of-stack
				task_stack[agent].emplace_front(composite_task->get_subtask(task_stack[agent].front().subtask),
												composite_task->subtask_bindings(agent, task_stack[agent].front().parameters, state, task_stack[agent].front().subtask));
			}
			actions[agent] = dynamic_cast<PrimTask*>(task_stack[agent].front().task)->primitive_action();
		}

	return actions;
}


void HierLearner::print_hierarchy (const string& filename) const
{	ofstream output((filename + ".gv").c_str());
	if (!output.is_open())
		throw HierException(__FILE__, __LINE__, "Unable to write hierarchy to graphviz file.");

	output << "digraph hierarchy {\n";
	output << "node [shape=rectangle]\n";
	output << "edge [arrowhead=empty]\n\n";
	for (const auto& task : Task_list)
		output << task->print_dot() << endl;
	output << "}\n";
	output.close();
	if (system(("dot -Tpdf -o " + filename + ".pdf " + filename + ".gv").c_str()))
		cerr << "(" + to_string(__FILE__) + ":" + to_string(__LINE__) + ")\n";
}


void HierLearner::write_text_file (const string& filename, const MDP& mdp) const
{	ofstream output(filename.c_str());
	if (!output.is_open())
		throw HierException(__FILE__, __LINE__, "Unable to write to file: " + filename);

	output << "MDP: " << mdp.name() << endl;

	// Task value functions
	const State& state = mdp.state();
	for (const auto& task : Task_list)
		output << task->print(state) << endl;

	output.close();
}


HierLearner::~HierLearner ()
{	for (auto& task : Task_list)
		delete task;
}
