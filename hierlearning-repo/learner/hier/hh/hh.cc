
/**********************************************************************************************

   	HIERARCHICAL H LEARNER (with TOTAL REWARD DECOMPOSITION)
		Neville Mehta

***********************************************************************************************/


#include <algorithm>
#include <fstream>
#include <sstream>
#include "hh_composite_task.h"
#include "hh_root_task.h"
#include "hh_primitive_task.h"
#include "hh.h"


valarray<double>* HH_CompositeTask::_avg_reward;
valarray<double>* HH_CompositeTask::_avg_time;


HH::HH (const string& learner_name, const MDP& mdp, const unique_ptr<Hierarchy>& hierarchy, const unsigned& run_index) : HierLearner(learner_name, mdp)
{	HH_CompositeTask::set_rho(&avg_reward, &avg_time);

	if (hierarchy)
		hierarchy->task_hierarchy_builder<HH_CompositeTask, HH_PrimTask, HH_RootTask>(mdp, Task_list);
	else
		flat_hierarchy_designer<HH_CompositeTask, HH_PrimTask, HH_RootTask>(mdp);
//		task_hierarchy_designer<HH_CompositeTask, HH_PrimTask, HH_RootTask>("taxi:manual", mdp);

	create_directory("output/" + mdp.name() + "/hierarchies");
	print_hierarchy("output/" + mdp.name() + "/hierarchies/hierarchy_" + to_string(run_index));
}


// Initialize the agent's innards
void HH::initialize (const MDP& mdp)
{	HierLearner::initialize(mdp);
	avg_reward.resize(mdp.state().num_agents(), 0.0);
	avg_time.resize(mdp.state().num_agents(), 0.0);
}


// Update the agent's innards
void HH::update (const State& next_state, const valarray<double>& reward, const valarray<double>& elapsed_time)
{	for (unsigned agent = 0; agent < next_state.num_agents(); ++agent)
		if (next_state.action_complete(agent) && !task_stack[agent].empty())
		{	HH_PrimTask* hh_primtask = dynamic_cast<HH_PrimTask*>(task_stack[agent].front().task);
			if (!hh_primtask)
				throw HierException(__FILE__, __LINE__, "Primitive task expected.");

			// Updating the primitive task and popping it off the task stack
			hh_primtask->update(agent, *task_stack[agent].front().state, reward[agent], elapsed_time[agent]);
			task_stack[agent].pop_front();

			// Updating the primitive task's parent
			dynamic_cast<HH_CompositeTask*>(task_stack[agent].front().task)->update(agent, task_stack[agent].front().parameters, *task_stack[agent].front().state, task_stack[agent].front().subtask, next_state);

			// Surfing through the task stack for tasks terminated in 'next_state'
			auto task_itr = task_stack[agent].begin();   // Top-of-stack
			auto task_itr_root = (++task_stack[agent].crbegin()).base();   // Root task iterator address
			while (task_itr != task_itr_root)
			{	if (dynamic_cast<CompositeTask*>(task_itr->task)->terminated(agent, task_itr->parameters, next_state))
				{	// Removing this task and all its children from the stack
					task_stack[agent].erase(task_stack[agent].begin(), ++task_itr);

					// Updating the parent task
					dynamic_cast<HH_CompositeTask*>(task_itr->task)->update(agent, task_itr->parameters, *task_itr->state, task_itr->subtask, next_state);
				}
				else
					++task_itr;
			}
		}
}
