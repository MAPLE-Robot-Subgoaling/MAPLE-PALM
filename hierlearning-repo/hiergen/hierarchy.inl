
/*************************************************

	HIERARCHICAL ANALYSIS
		Neville Mehta

**************************************************/

#pragma once

#include <iterator>


template <typename CTask, typename PTask, typename RTask>
inline void Hierarchy::task_hierarchy_builder (const MDP& mdp, list<Task*>& Task_list) const
{	map<Hierarchy*,CompositeTask::Subtask> memoizer;
	task_hierarchy_builder<CTask, PTask, RTask>(mdp, Task_list, memoizer);
}


template <typename CTask, typename PTask, typename RTask>
CompositeTask::Subtask Hierarchy::task_hierarchy_builder (const MDP& mdp, list<Task*>& Task_list, map<Hierarchy*,CompositeTask::Subtask>& memoizer) const
{	CompositeTask::Subtask task;
	if (_subtasks.empty())   // Primitive subtask
	{	// Check for matching task
		for (const auto& tl : Task_list)
			if (tl->primitive() && tl->abstraction() == _abstraction && dynamic_cast<PTask*>(tl)->primitive_action() == _primitive_action)
				return tl;

		// Create new subtask
		task.link = new PTask(mdp.print_action(_primitive_action), abstraction_string(_abstraction, mdp), _primitive_action, mdp);
	}
	else   // Composite subtask
	{	vector<CompositeTask::Subtask> subtasks;
		set<int> abstraction(_abstraction);
		for (unsigned h_t = 0; h_t < _subtasks.size(); ++h_t)
		{	CompositeTask::Subtask subtask;
			map<Hierarchy*,CompositeTask::Subtask>::const_iterator s_itr = memoizer.find(_subtasks[h_t]);
			if (s_itr != memoizer.end())
				subtask = s_itr->second;
			else
			{	subtask = _subtasks[h_t]->task_hierarchy_builder<CTask, PTask, RTask>(mdp, Task_list, memoizer);
				memoizer[_subtasks[h_t]] = subtask;
			}

			unsigned t;
			for (t = 0; t < subtasks.size(); ++t)
				if (subtasks[t] == subtask)
					break;
			if (t == subtasks.size())
			{	subtasks.push_back(subtask);
				set<int> subtask_abstraction = subtask.link->abstraction();
				abstraction.insert(subtask_abstraction.begin(), subtask_abstraction.end());
			}
		}

		// New termination = OR(old termination, NOT(admissibility))
		Expression termination(_admissibility);
		termination.negate();
		termination.disjunct(_termination);

		// Parameterization of the termination condition: parameter variables are those in the termination condition but not in the abstraction
		const set<int> termination_variables = termination.variables();
		vector<int> parameter_variables;
		if (!_is_root)   // No parameterization for the Root task
			set_difference(termination_variables.begin(), termination_variables.end(), abstraction.begin(), abstraction.end(), inserter(parameter_variables, parameter_variables.end()));
		termination.parameterize(parameter_variables);

		// Determine agent variables in the termination condition
		vector<int> agent_variables;
		for (const auto& term_var : termination_variables)
			if (mdp.state().variable_name(term_var).substr(0,5) == "agent")
				agent_variables.push_back(term_var);
		termination.set_agent_variables(agent_variables);

		// Check for matching tasks
		for (const auto& tl : Task_list)
			if (!tl->primitive() && combinable_tasks(subtasks, tl))
			{	CompositeTask* comp_task = dynamic_cast<CompositeTask*>(tl);
				map<int,int> parameter_map;
				if (termination.parameter_binding_equality(comp_task->termination_expression(), parameter_map))
				{	// Combine the abstraction
					set<int> temp_abstraction(tl->abstraction());
					abstraction.insert(temp_abstraction.begin(), temp_abstraction.end());

					// Combine subtasks
					for (unsigned i = 0; i < subtasks.size(); ++i)
					{	if (subtasks[i].link == tl)   // Avoiding self-referencing
							continue;

						bool unique_subtask = true;
						for (unsigned j = 0; j < comp_task->num_subtasks(); ++j)
							if (subtasks[i] == comp_task->get_subtask(j))
							{	unique_subtask = false;
								break;
							}

						if (unique_subtask)
							comp_task->add_subtask(subtasks[i], mdp);
					}

					CompositeTask::Subtask reused_task(tl);
					if (!parameter_map.empty())
					{	vector<int> parameter_size;
						for (unsigned p = 0; p < parameter_variables.size(); ++p)
						{	parameter_size.push_back(mdp.state().variable_size(parameter_variables[parameter_map[p]]));
							reused_task.parameters += mdp.state().variable_name(parameter_variables[parameter_map[p]]) + ", ";
						}
						reused_task.parameters.erase(reused_task.parameters.size() - 2);
						tl->update_parameters(parameter_size);
					}
					return reused_task;
				}
			}

		// Task parameterization
		string parameter_size_str;
		for (auto v_itr = parameter_variables.cbegin(); v_itr != parameter_variables.cend(); ++v_itr)
		{	if (v_itr != parameter_variables.begin())
			{	parameter_size_str += ", ";
				task.parameters += ", ";
			}
			parameter_size_str += to_string(mdp.state().variable_size(*v_itr));
			task.parameters += mdp.state().variable_name(*v_itr);
		}

		// Create new subtask
		string task_name;
		if (_is_root)
		{	task_name = "Root";
			task.link = new RTask(task_name, parameter_size_str, abstraction_string(abstraction, mdp), subtasks, nullptr, termination, mdp);
		}
		else
		{	task_name = "Task" + to_string(Task_list.size());
			task.link = new CTask(task_name, parameter_size_str, abstraction_string(abstraction, mdp), subtasks, nullptr, termination, mdp);
		}
	}

	// Add new task
	Task_list.push_front(task.link);
	return task;
}
