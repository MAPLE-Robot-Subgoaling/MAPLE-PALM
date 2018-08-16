
/*****************************************************************************************

   	MAXQ LEARNER
		Neville Mehta

******************************************************************************************/


#include <algorithm>
#include <fstream>
#include <sstream>
#include "maxq_composite_task.h"
#include "maxq_primitive_task.h"
#include "maxq.h"


MaxQ::MaxQ (const string& learner_name, const MDP& mdp, const unique_ptr<Hierarchy>& hierarchy, const unsigned& run_index) : HierLearner(learner_name, mdp)
{	if (hierarchy)
		hierarchy->task_hierarchy_builder<MaxQ_CompositeTask, MaxQ_PrimTask, MaxQ_CompositeTask>(mdp, Task_list);
	else
		flat_hierarchy_designer<MaxQ_CompositeTask, MaxQ_PrimTask, MaxQ_CompositeTask>(mdp);
//		task_hierarchy_designer<MaxQ_CompositeTask, MaxQ_PrimTask, MaxQ_CompositeTask>("taxi:manual", mdp);

	create_directory("output/" + mdp.name() + "/hierarchies");
	print_hierarchy("output/" + mdp.name() + "/hierarchies/hierarchy_" + to_string(run_index));
}


// Update the agent's innards
void MaxQ::update (const State& next_state, const valarray<double>& reward, const valarray<double>& duration)
{	for (unsigned agent = 0; agent < next_state.num_agents(); ++agent)
		if (next_state.action_complete(agent) && !task_stack[agent].empty())
		{	MaxQ_PrimTask* maxq_primtask = dynamic_cast<MaxQ_PrimTask*>(task_stack[agent].front().task);
			if (!maxq_primtask)
				throw HierException(__FILE__, __LINE__, "Primitive task expected.");

			// Updating the primitive task and popping it off the task stack
			maxq_primtask->update(agent, *task_stack[agent].front().state, reward[agent]);
			task_stack[agent].pop_front();

			// Updating the primitive task's parent
			dynamic_cast<MaxQ_CompositeTask*>(task_stack[agent].front().task)->update(agent, task_stack[agent].front().parameters, *task_stack[agent].front().state, task_stack[agent].front().subtask, next_state, duration[agent]);

			// Surfing through the task stack for tasks terminated in 'next_state'
			auto task_itr = task_stack[agent].begin();   // Top-of-stack
			auto task_itr_root = (++task_stack[agent].crbegin()).base();   // Root task iterator address
			while (task_itr != task_itr_root)
			{	task_itr->time += duration[agent];

				if (dynamic_cast<CompositeTask*>(task_itr->task)->terminated(agent, task_itr->parameters, next_state))
				{	double child_duration = task_itr->time;   // Sending the current task's duration up the stack
					task_stack[agent].erase(task_stack[agent].begin(), ++task_itr);   // Removing this task and all its children from the stack

					// Updating the parent task
					dynamic_cast<MaxQ_CompositeTask*>(task_itr->task)->update(agent, task_itr->parameters, *task_itr->state, task_itr->subtask, next_state, child_duration);
				}
				else
					++task_itr;
			}
		}
}
