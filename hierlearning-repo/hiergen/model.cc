
/*************************************************

	MODEL INTERFACE
		Neville Mehta

**************************************************/


#include <algorithm>
#include <fstream>
#include <iterator>
#include "model.h"


#if defined(NDEBUG)
const bool display = false;
#else
const bool display = true;
#endif

double pruning_confidence = 0;   // Closer to zero = more pruning (Weka complains below 1e-6 and above 1); 0 = unpruned


ModelTree::ModelTree (const string& filename, const State& state) : leaf(0)
{	ifstream model_file(filename.c_str());
	if (!model_file.is_open())
		throw HierException(__FILE__, __LINE__, "Model file " + filename + " does not exist.");

	list<string> model_file_lines;
	while (!model_file.eof())
	{	string line;
		getline_os(model_file, line);
		if (!line.empty())
			model_file_lines.push_back(line);
	}
	read_file(model_file_lines, state, 0);
	model_file.close();
}


ModelTree::ModelTree (list<string>& model_file_lines, const State& state, const int& level) : leaf(0)
{	read_file(model_file_lines, state, level);
}


void ModelTree::read_file (list<string>& model_file_lines, const State& state, const int& level)
{	while (!model_file_lines.empty())
	{	string line = model_file_lines.front();
		model_file_lines.pop_front();

		// Determine recursion level of current line
		int count = 0;
		size_t index = line.find_first_of("|");
		while (index != string::npos)
		{	++count;
			index = line.find_first_of("|", index + 1);
		}
		if (count < level)
		{	model_file_lines.push_front(line);
			break;
		}

		index = line.find_first_of(":");
		if (index == string::npos)   // No leaf value exists
			choices.emplace_back(Expression(line.substr(line.find_first_not_of(" |")), state), ModelTree(model_file_lines, state, level + 1));
		else
		{	size_t start = line.find_first_not_of(" |");
			if (index - start)
				choices.emplace_back(Expression(line.substr(start, index - start), state), ModelTree(from_string<int>(line.substr(index + 2))));
			else
				leaf = from_string<int>(line.substr(index + 2));
		}
	}
}


int ModelTree::true_choice (const State& state) const
{	for (unsigned i = 0; i < choices.size(); ++i)
		if (choices[i].first.evaluate(state))
			return i;

	throw HierException(__FILE__, __LINE__, "No satisfying path in action model tree.");
}


int ModelTree::true_choice (const map<int,int>& variable_value) const
{	for (unsigned i = 0; i < choices.size(); ++i)
		if (choices[i].first.evaluate(variable_value))
			return i;

	throw HierException(__FILE__, __LINE__, "No satisfying path in action model tree.");
}


int ModelTree::get_value (const State& state) const
{	if (is_leaf())
		return leaf;

	return choices[true_choice(state)].second.get_value(state);
}


int ModelTree::get_value (const map<int,int>& variable_value) const
{	if (is_leaf())
		return leaf;

	return choices[true_choice(variable_value)].second.get_value(variable_value);
}


set<int> ModelTree::leaf_values () const
{	if (is_leaf())
		return make_set<int>(1, leaf);

	set<int> all_leaf_values;
	for (const auto& choice : choices)
	{	set<int> child_leaf_values = choice.second.leaf_values();
		all_leaf_values.insert(child_leaf_values.begin(), child_leaf_values.end());
	}
	return all_leaf_values;
}



bool ModelTree::is_persistent (const int& variable) const
{	// Presumes that every split is based on the domain values of a single variable, e.g., v0 = 0, v0 = 1, v0 = 2, ...

	if (is_leaf())
		return false;

	for (const auto& choice : choices)
	{	set<int> lhs_variable = choice.first.variables();
		if (lhs_variable.size() > 1 || *lhs_variable.begin() != variable)
			return false;

		set<int> leaf_values = choice.second.leaf_values();
		if (leaf_values.size() > 1 || *leaf_values.begin() != choice.first.rhs_value())
			return false;
	}
	return true;
}


set<int> ModelTree::checked_variables () const
{	if (is_leaf() || leaf_values().size() == 1)   // Checking for redundant subtrees
		return set<int>();

	set<int> variables;
	for (const auto& choice : choices)
	{	set<int> vars = choice.first.variables();
		variables.insert(vars.begin(), vars.end());
		vars = choice.second.checked_variables();
		variables.insert(vars.begin(), vars.end());
	}
	return variables;
}


set<int> ModelTree::context_checked_variables (const map<int,int>& variable_value) const
{	if (is_leaf() || leaf_values().size() == 1)   // Checking for redundant subtrees
		return set<int>();

	int tc = true_choice(variable_value);
	set<int> abstraction = choices[tc].first.variables();
	set<int> child_abstraction = choices[tc].second.context_checked_variables(variable_value);
	abstraction.insert(child_abstraction.begin(), child_abstraction.end());
	return abstraction;
}


string ModelTree::print_gv_cluster (const string& node_prefix, int& id) const
{	string model_str;

	if (is_leaf())
		model_str += node_prefix + to_string(id) + " [label=\"" + to_string(leaf) + "\"]\n";
	else
	{	// Values on edges only if all are equality tests for the same lhs variable
		bool value_edge_label = false;
		Expression internal_node = choices.front().first;
		if (internal_node.equality())
		{	value_edge_label = true;
			for (unsigned i = 1; i < choices.size(); ++i)
				if (choices[i].first.lhs_expression() != choices.front().first.lhs_expression())
				{	value_edge_label = false;
					break;
				}
		}

		int parent_id = id;
		if (value_edge_label)
		{	model_str += node_prefix + to_string(parent_id) + " [label=<" + choices.front().first.lhs_expression().print_html() + ">]\n";
			for (const auto& choice : choices)
			{	model_str += node_prefix + to_string(parent_id) + " -> " + node_prefix + to_string(++id) + " [label=<" + choice.first.rhs_expression().print_html() + ">]\n";
				model_str += choice.second.print_gv_cluster(node_prefix, id);
			}
		}
		else
		{	model_str += node_prefix + to_string(parent_id) + " [label=\"\"]\n";
			for (const auto& choice : choices)
			{	model_str += node_prefix + to_string(parent_id) + " -> " + node_prefix + to_string(++id) + " [label=<" + choice.first.print_html() + ">]\n";
				model_str += choice.second.print_gv_cluster(node_prefix, id);
			}
		}
	}

	return model_str;
}


//-------------------------------------------------------------------------------------------------


ActionModel::ActionModel (const string& trajectory_filename, const string& model_directory, const MDP& mdp)
{	string action_model_directory;
	if (!model_directory.empty())
		action_model_directory = model_directory;
	else
	{	size_t i = trajectory_filename.find_last_of("\\/");
		action_model_directory = "output/" + mdp.name() + "/models_" + trajectory_filename.substr(i + 1, trajectory_filename.find_first_of(".") - i - 1);

		create_directory(action_model_directory);
		if (system(("python model_builder.py " + trajectory_filename + " " + action_model_directory + " " + to_string(pruning_confidence) + " " + (display ? "True" : "False")).c_str()))
			throw HierException(__FILE__, __LINE__, "There was a problem building action models.");
	}
	read(action_model_directory, mdp);
	if (display && model_directory.empty()) print_gv(action_model_directory, mdp);
}


ActionModel::ActionModel (const vector<vector<unique_ptr<State_Action_Reward>>>& input_trajectories, const string& model_directory, const unsigned& run_index, const MDP& mdp)
{	string action_model_directory;
	if (!model_directory.empty())
		action_model_directory = model_directory;
	else
	{	action_model_directory = "output/" + mdp.name() + "/models_internal_trajectory_" + to_string(run_index);
		create_directory(action_model_directory);

		string trajectory_filename = "output/" + mdp.name() + "/internal_trajectory_" + to_string(run_index) + ".out";
		ofstream trajectory_file(trajectory_filename.c_str());
		if (!trajectory_file.is_open())
			throw HierException(__FILE__, __LINE__, "Unable to write trajectories to file.");

		// Header information
		vector<int> mdp_actions = mdp.actions();
		for (unsigned a = 0; a < mdp_actions.size(); ++a)
		{	if (a > 0)
				trajectory_file << " ";
			trajectory_file << mdp.print_action(mdp_actions[a]);
		}
		trajectory_file << endl;
		for (unsigned a = 0; a < mdp_actions.size(); ++a)
		{	if (a > 0)
				trajectory_file << " ";
			trajectory_file << mdp_actions[a];
		}
		trajectory_file << endl;
		vector<int> state_variables = mdp.state().variables();
		for (auto v = state_variables.cbegin(); v != state_variables.cend(); ++v)
		{	if (v != state_variables.begin())
				trajectory_file << " ";
			trajectory_file << mdp.state().variable_name(*v);
		}
		trajectory_file << endl;
		for (auto v = state_variables.cbegin(); v != state_variables.cend(); ++v)
		{	if (v != state_variables.begin())
				trajectory_file << " ";
			trajectory_file << mdp.state().variable_size(*v);
		}
		trajectory_file << "\n#\n";

		for (const auto& trajectory : input_trajectories)
			if (!trajectory.empty())
			{	for (auto sar = trajectory.cbegin(); sar != trajectory.cend(); ++sar)
				{	if (sar + 1 != trajectory.end())
						trajectory_file << (*sar)->state->print() << endl << (*sar)->action[0] << endl << (*sar)->reward[0] << endl;
					else
						trajectory_file << (*sar)->state->print() << endl;
				}
				trajectory_file << "#\n";
			}
		trajectory_file.close();

		if (system(("python model_builder.py " + trajectory_filename + " " + action_model_directory + " " + to_string(pruning_confidence) + " " + (display ? "True" : "False")).c_str()))
			throw HierException(__FILE__, __LINE__, "There was a problem building action models.");

		//remove(trajectory_filename.c_str());
	}
	read(action_model_directory, mdp);
	if (model_directory.empty())
	{	if (display)
			print_gv(action_model_directory, mdp);
		else
			remove_directory(action_model_directory.c_str());
	}
}


void ActionModel::read (const string& directory, const MDP& mdp)
{	vector<int> mdp_actions = mdp.actions();
	vector<int> state_variables = mdp.state().variables();

	if (display) cout << "Reading models ...\n";
	for (const auto& mdp_action : mdp_actions)
	{	string action_name = mdp.print_action(mdp_action);
		if (display) cout << action_name << endl;
		for (const auto& var : state_variables)
		{	string filename = action_name + "__" + mdp.state().variable_name(var) + ".model";
			_model[mdp_action].transition[var] = ModelTree(directory + "/" + filename, mdp.state());
		}

		string filename = action_name + "__reward.model";
		_model[mdp_action].reward = ModelTree(directory + "/" + filename, mdp.state());
	}
	if (display) cout << "Done.\n";

	compute_checked_variables();
}


void ActionModel::compute_checked_variables ()
{	for (auto& am : _model)
	{	// Transition
		for (const auto& var : am.second.transition)
			if (!var.second.is_persistent(var.first))
			{	set<int> checked_per_variable = var.second.checked_variables();
				am.second.transition_checked_variables.insert(checked_per_variable.begin(), checked_per_variable.end());
			}

		// Reward
		am.second.reward_checked_variables = am.second.reward.checked_variables();
		am.second.reward_checked_variables_depth_2 = closure_next_stage(am, am.second.reward_checked_variables);
	}
}


set<int> ActionModel::closure_next_stage (const pair<int, SingleActionModel>& am, const set<int>& closure_variables) const
{	set<int> next_closure_variables = closure_variables;

	for (const auto& var : closure_variables)
	{	auto v = am.second.transition.find(var);
		if (!v->second.is_persistent(v->first))
		{	set<int> checked_per_variable = v->second.checked_variables();
			next_closure_variables.insert(checked_per_variable.begin(), checked_per_variable.end());
		}
	}

	return next_closure_variables;
}


set<int> ActionModel::closure (const set<int>& actions, const set<int>& variables) const
{	set<int> closure_variables = variables;

	// Collect reward variables from all actions
	for (const auto& action : actions)
	{	auto am_itr = _model.find(action);
		if (am_itr == _model.end())
			throw HierException(__FILE__, __LINE__, "No model exists for the provided action.");

		closure_variables.insert(am_itr->second.reward_checked_variables.begin(), am_itr->second.reward_checked_variables.end());
	}

	while (true)
	{	set<int> next_stage_closure_variables = closure_variables;
		for (const auto& action : actions)
		{	set<int> next_stage_closure_variables_per_action = closure_next_stage(*_model.find(action), closure_variables);
			next_stage_closure_variables.insert(next_stage_closure_variables_per_action.begin(), next_stage_closure_variables_per_action.end());
		}

		if (next_stage_closure_variables == closure_variables)
			break;

		closure_variables = next_stage_closure_variables;
	}

	return closure_variables;
}


set<int> ActionModel::reward_variables (const int& action) const
{	auto am_itr = _model.find(action);
	if (am_itr == _model.end())
		throw HierException(__FILE__, __LINE__, "No model exists for the provided action.");

	return am_itr->second.reward_checked_variables_depth_2;
}


set<int> ActionModel::checked_variables (const int& action) const
{	auto am_itr = _model.find(action);
	if (am_itr == _model.end())
		throw HierException(__FILE__, __LINE__, "No model exists for the provided action.");

	return am_itr->second.transition_checked_variables;
}


set<int> ActionModel::context_checked_variables (const int& action, const map<int,int>& variable_value) const
{	auto am_itr = _model.find(action);
	if (am_itr == _model.end())
		throw HierException(__FILE__, __LINE__, "No model exists for the provided action.");
	
	set<int> checked_variables;

	// Transition
	for (const auto& v : am_itr->second.transition)
		if (!v.second.is_persistent(v.first))
		{	set<int> checked_per_variable = v.second.context_checked_variables(variable_value);
			checked_variables.insert(checked_per_variable.begin(), checked_per_variable.end());
		}

	// Reward
	set<int> reward_variables = am_itr->second.reward.context_checked_variables(variable_value);
	checked_variables.insert(reward_variables.begin(), reward_variables.end());

	return checked_variables;
}


set<int> ActionModel::context_changed_variables (const int& action, const map<int,int>& variable_value) const
{	// Doesn't apply to stochastic outcomes

	auto am_itr = _model.find(action);
	if (am_itr == _model.end())
		throw HierException(__FILE__, __LINE__, "No model exists for the provided action.");

	set<int> changed_variables;
	for (const auto& v : am_itr->second.transition)
		if (variable_value.find(v.first)->second != v.second.get_value(variable_value))   // Variable's value changes according to model
			changed_variables.insert(v.first);
	return changed_variables;
}


void ActionModel::print_gv (const string& directory, const MDP& mdp) const
{	if (display) cout << "Printing models ...\n";
	for (const auto& am : _model)
	{	string gv_filename = directory + "/" + mdp.print_action(am.first);
		if (display) cout << gv_filename << ".pdf\n";
		ofstream model_file((gv_filename + ".gv").c_str());
		if (!model_file.is_open())
			throw HierException(__FILE__, __LINE__, "Unable to print action model.");

		model_file << "digraph {\nedge [arrowhead=empty]\n\n";
		for (const auto& m : am.second.transition)
		{	model_file << "subgraph cluster_" << mdp.state().variable_name(m.first) << " {\nlabel=\"" << mdp.state().variable_name(m.first) << "\"\n";
			model_file << m.second.print_gv_cluster(mdp.state().variable_name(m.first) + "_n");
			model_file << "}\n\n";
		}
		model_file << "subgraph cluster_reward {\nlabel=\"reward\"\n";
		model_file << am.second.reward.print_gv_cluster("reward_n");
		model_file << "}\n}\n";
		model_file.close();

		if (system(("dot -Tpdf -o " + gv_filename + ".pdf " + gv_filename + ".gv").c_str()))
			cerr << "(" + to_string(__FILE__) + ":" + to_string(__LINE__) + ")\n";
	}
	if (display) cout << "Done.\n";
}


void ActionModel::print_context_precondition (const MDP& mdp) const
{	ofstream precondition_file("precondition.txt");
	if (!precondition_file.is_open())
		throw HierException(__FILE__, __LINE__, "Unable to print precondition file.");

	map<int,int> variable_size;
	vector<int> state_variables = mdp.state().variables();
	for (const auto& var : state_variables)
		variable_size[var] = mdp.state().variable_size(var);

	vector<int> mdp_actions = mdp.actions();
	for (unsigned a = 0; a < mdp_actions.size(); ++a)
	{	map<int,int> state;
		for (const auto& var : state_variables)
			state[var] = 0;

		precondition_file << mdp.print_action(mdp_actions[a]) << endl;
		do
		{	set<int> precondition;
			try
			{	precondition = context_checked_variables(a, state);
			}
			catch (...)   // ModelTree throws an exception when no path is true
			{}

			if (!precondition.empty())
			{	precondition_file << state << " : ";
				for (const auto& p : precondition)
					precondition_file << p << " ";
				precondition_file << endl;
			}

			bool carry = true;
			for (auto v = state.begin(); v != state.end() && carry; ++v)
			{	++v->second;
				map<int,int>::const_iterator u = v;
				if (v->second == variable_size[v->first] && ++u != state.end())
				{	v->second = 0;
					carry = true;
				}
				else
					carry = false;
			}
		} while (state.rbegin()->second < (int)variable_size[state.rbegin()->first]);
		precondition_file << endl << endl;
	}

	precondition_file.close();
}
