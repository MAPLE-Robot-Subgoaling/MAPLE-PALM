
/*************************************************

	HIERARCHICAL ANALYSIS
		Neville Mehta

**************************************************/


#include <algorithm>
#include <fstream>
#include "../lib/common.h"
#include "../learner/hier/generic/primitive_task.h"
#include "hierarchy.h"


#if defined(NDEBUG)
const bool display = false;
#else
const bool display = true;
#endif


Hierarchy::Hierarchy (const string& trajectory_filename, const string& model_directory, const MDP& mdp) : _is_root(true), _primitive_action(0)
{	ifstream tf(trajectory_filename.c_str());
	if (!tf.is_open())
		throw HierException(__FILE__, __LINE__, "Unable to open " + trajectory_filename + ".");

	ActionModel action_model(trajectory_filename, model_directory, mdp);

	// Trajectory file header
	string header;
	getline_os(tf, header);   // Actions
	getline_os(tf, header);   // Action indices
	getline_os(tf, header);   // Variables
	vector<string> vars = tokenize(header, " ");
	if (vars.size() != mdp.state().variables().size())
		throw HierException(__FILE__, __LINE__, "Trajectory variables do not match that of the MDP.");
	while (header != "#")
		getline_os(tf, header);

	if (display) cout << "Annotating trajectories ...";
	vector<Trajectory> trajectories;
	while (true)
	{	char c = 0;
		while (tf >> c && c == '#');   // Gobble up empty trajectories
		if (tf.eof())
			break;
		tf.putback(c);

		if (display) cout << " " << trajectories.size() + 1;
		trajectories.emplace_back(tf, mdp, action_model);
		if (trajectories.size() > 1 && extract_map_keys(trajectories.back().extract_goal_state()) != extract_map_keys(trajectories[trajectories.size() - 2].extract_goal_state()))
			throw HierException(__FILE__, __LINE__, "Malformed state detected.");
	}
	tf.close();
	if (display) cout << ".\n";
	if (trajectories.empty())
		throw HierException(__FILE__, __LINE__, "No input trajectories in " + trajectory_filename + ".");

	if (display) print_cats("output/" + mdp.name() + "/cats", trajectories, mdp);

	termination_condition(trajectories);
	state_abstraction(trajectories, action_model, _termination.variables());

	hierarchicalize(trajectories, action_model);
}


Hierarchy::Hierarchy (const vector<vector<unique_ptr<State_Action_Reward>>>& input_trajectories, const string& model_directory, const unsigned& run_index, const MDP& mdp) : _is_root(true), _primitive_action(-1)
{	if (input_trajectories.empty())
		throw HierException(__FILE__, __LINE__, "No input trajectories.");

	ActionModel action_model(input_trajectories, model_directory, run_index, mdp);

	if (display) cout << "Annotating trajectories ...";
	vector<Trajectory> trajectories;
	for (const auto& trajectory : input_trajectories)
		if (!trajectory.empty())
		{	if (display) cout << " " << trajectories.size() + 1;
			trajectories.emplace_back(trajectory, action_model);
			if (trajectories.size() > 1 && extract_map_keys(trajectories.back().extract_goal_state()) != extract_map_keys(trajectories[trajectories.size() - 2].extract_goal_state()))
				throw HierException(__FILE__, __LINE__, "Malformed state detected.");
		}
	if (display) cout << "done.\n";

	if (display) print_cats("output/" + mdp.name() + "/cats_internal", trajectories, mdp);

	termination_condition(trajectories);
	state_abstraction(trajectories, action_model, _termination.variables());

	hierarchicalize(trajectories, action_model);
}


void Hierarchy::state_abstraction (const vector<Trajectory>& trajectories, const ActionModel& action_model, const set<int>& goal_variables)
{	set<int> actions;
	for (const auto& trajectory : trajectories)
	{	set<int> trajectory_actions = trajectory.extract_actions();
		actions.insert(trajectory_actions.begin(), trajectory_actions.end());
	}

	_abstraction = action_model.closure(actions, goal_variables);
}


void Hierarchy::termination_condition (const vector<Trajectory>& trajectories)
{	vector<map<int,int>> final_states;
	for (const auto& trajectory : trajectories)
		final_states.push_back(trajectory.extract_goal_state());
	_termination = Expression(final_states, trajectories[0].goal_variables());
}


void Hierarchy::hierarchicalize (const vector<Trajectory>& trajectories, const ActionModel& action_model)
{	if (!_termination.empty())
	{	CatScan catscan(trajectories, _termination);
		if (catscan.decomposed())
		{	_subtasks = incorporate_subtasks(catscan, action_model);
			return;
		}
	}

	// No decomposition; include primitive actions as children
	map<int,set<int>> action_abstractions = extract_action_abstractions(trajectories, action_model);
	for (const auto& aa : action_abstractions)
	{	_subtasks.push_back(new Hierarchy(aa.first, aa.second));
		_subtask_library.emplace_back(0, _subtasks.back());
	}
}


vector<Hierarchy*> Hierarchy::incorporate_subtasks (const CatScan& catscan, const ActionModel& action_model, const unsigned& index, const bool& precondition_ordering, const bool& admissibility_wrapper)
{	vector<Hierarchy*> subtasks;
	if (precondition_ordering)
	{	set<unsigned> child_task_indices = catscan.subtask_indices(index);
		for (const auto& child_index : child_task_indices)
		{	vector<pair<unsigned,Hierarchy*>>::const_iterator sl_itr = find_if(_subtask_library.begin(), _subtask_library.end(), [&](const pair<unsigned, Hierarchy*>& p){ return p.first == child_index; });
			if (sl_itr != _subtask_library.end())
				subtasks.push_back(sl_itr->second);
			else
			{	vector<Hierarchy*> child_tasks = incorporate_subtasks(catscan, action_model, child_index, precondition_ordering, admissibility_wrapper);
				Hierarchy* hierarchy = new Hierarchy(set<int>(), Expression(), catscan.termination(child_index), catscan.subtrajectories(child_index), action_model);
//				Hierarchy* hierarchy = new Hierarchy(set<int>(), Expression(), Expression(), catscan.subtrajectories(*c_itr), action_model);   // To incorporate the primitive actions without further decomposition
				_subtask_library.emplace_back(0, hierarchy);
				if (admissibility_wrapper && !child_tasks.empty() && !catscan.admissibility(child_index).empty() && hierarchy->_subtasks.size() == 1 && hierarchy->_subtasks.front()->_subtasks.empty())
				{	// Only wrap single primitive action with admissibility
					hierarchy->_abstraction = hierarchy->_subtasks.front()->_abstraction;
					hierarchy->_admissibility = catscan.admissibility(child_index);
					child_tasks.push_back(hierarchy);
				}
				else
					child_tasks.insert(child_tasks.end(), hierarchy->_subtasks.begin(), hierarchy->_subtasks.end());

				if (catscan.termination(child_index).empty() && (!admissibility_wrapper || catscan.admissibility(child_index).empty() || child_tasks.size() != 1 || !child_tasks.front()->_subtasks.empty()))
					subtasks.insert(subtasks.end(), child_tasks.begin(), child_tasks.end());
				else
				{	if (admissibility_wrapper && !catscan.admissibility(child_index).empty() && child_tasks.size() == 1 && child_tasks.front()->_subtasks.empty())
						subtasks.push_back(new Hierarchy(child_tasks.front()->_abstraction, catscan.admissibility(child_index), Expression(), child_tasks));
					else
						subtasks.push_back(new Hierarchy(catscan.abstraction(child_index, action_model), Expression(), catscan.termination(child_index), child_tasks));
					_subtask_library.emplace_back(child_index, subtasks.back());
				}
			}
		}
	}
	else
		for (unsigned i = 1; i <= catscan.num_subtasks(); ++i)
		{	Hierarchy* hierarchy = new Hierarchy(catscan.abstraction(i, action_model), catscan.admissibility(i), catscan.termination(i), catscan.subtrajectories(i), action_model);
			_subtask_library.push_back(make_pair(i, hierarchy));
			if (hierarchy->_subtasks.size() == 1 && hierarchy->_subtasks.front()->_subtasks.empty())
				hierarchy->_abstraction = hierarchy->_subtasks.front()->_abstraction;
			subtasks.push_back(hierarchy);
		}

	return subtasks;
}


map<int,set<int>> Hierarchy::extract_action_abstractions (const vector<Trajectory>& trajectories, const ActionModel& action_model) const
{	map<int,set<int>> action_abstractions;
	for (const auto& trajectory : trajectories)
	{	set<int> actions = trajectory.extract_actions();
		for (const auto& action : actions)
			if (action_abstractions.find(action) == action_abstractions.end())
				action_abstractions[action] = action_model.reward_variables(action);
	}
	return action_abstractions;
}


bool Hierarchy::combinable_tasks (vector<CompositeTask::Subtask>& subtasks, Task* target_task) const
{	// Combine if the tasks are not connected or target is an immediate child
	for (const auto& subtask : subtasks)
		if (connected_tasks(subtask.link, target_task) && subtask.link != target_task)
			return false;
	return true;
}


bool Hierarchy::connected_tasks (Task* source_task, Task* target_task) const
{	if (source_task == target_task)
		return true;
	
	if (!source_task->primitive())
	{	CompositeTask* comp_task = dynamic_cast<CompositeTask*>(source_task);
		for (unsigned t = 0; t < comp_task->num_subtasks(); ++t)
			if (connected_tasks(comp_task->get_subtask(t), target_task))
				return true;
	}
	return false;
}


string Hierarchy::abstraction_string (const set<int>& abstraction_variables, const MDP& mdp) const
{	// Stringifying the abstraction
	string abstraction_str;
	for (const auto& var : abstraction_variables)
		abstraction_str += mdp.state().variable_name(var) + ", ";
	if (!abstraction_str.empty())
		abstraction_str.erase(abstraction_str.size() - 2);
	return abstraction_str;
}


void Hierarchy::print_cats (const string& directory, const vector<Trajectory>& trajectories, const MDP& mdp) const
{	if (display) cout << "Printing cats ...";
	create_directory(directory);
	for (unsigned t = 0; t < trajectories.size(); ++t)
	{	if (display) cout << " " << t + 1;
		trajectories[t].print_gv(directory + "/cat_" + to_string(t + 1), mdp);
	}
	if (display) cout << ".\n";
}


Hierarchy::~Hierarchy ()
{	for (const auto& sl : _subtask_library)
		delete sl.second;
}
