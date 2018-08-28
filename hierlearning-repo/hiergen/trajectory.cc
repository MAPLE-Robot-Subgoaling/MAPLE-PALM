
/*************************************************

	CAUSALLY ANNOTATED TRAJECTORY
		Neville Mehta

**************************************************/


#include <algorithm>
#include <fstream>
#include "../lib/common.h"
#include "trajectory.h"



Trajectory::Trajectory (istream& in, const MDP& mdp, const ActionModel& action_model)
{	if (!in.eof())
	{	_trajectory.emplace_back(map<int,int>(), Start_Action);   // Insert Start action

		unique_ptr<State> mdp_state = mdp.state().copy();
		char c = 0;
		while (!in.eof() && c != '#')
		{	int action = End_Action;
			double reward = 0.0;

			mdp_state->read(in);
			if (!in.eof())
			{	in >> c;   // Check for trajectory separator symbol
				if (c != '#')
				{	in.putback(c);
					bool bb = in >> action;
					bool cc = in >> reward;
					if (!(bb) || !(cc))
						throw HierException(__FILE__, __LINE__, "Bad trajectory format.");
				}
			}
			map<int,int> state = mdp_state->variables_mapper();

			// Remove loops through the state space
			for (auto trj_itr = _trajectory.rbegin(); trj_itr != _trajectory.rend(); ++trj_itr)
				if (trj_itr->state == state)
				{	_trajectory.erase(trj_itr.base() - 1, _trajectory.end());
					break;
				}

			_trajectory.emplace_back(state, action, reward);
		}

		if (_trajectory.size() >= 3)
		{	causal_annotation(action_model, mdp.state().goal_variables(), mdp.state().transfer_variables());
			cleanup();
			return;
		}
	}

	throw HierException(__FILE__, __LINE__, "Unable to read the trajectory.");
}


Trajectory::Trajectory (const vector<unique_ptr<State_Action_Reward>>& sample_trajectory, const ActionModel& action_model)
{
	if (!sample_trajectory.empty())
	{	_trajectory.emplace_back(map<int,int>(), Start_Action);   // Insert Start action
		for (auto sar_itr = sample_trajectory.cbegin(); sar_itr != sample_trajectory.cend(); ++sar_itr)
		{	map<int,int> state = (*sar_itr)->state->variables_mapper();

			//cout << "NOT removing redundant loops in state space (for now)\n";
			// Removing redundant loops through the state space
			for (auto trj_itr = _trajectory.rbegin(); trj_itr != _trajectory.rend(); ++trj_itr)
				if (trj_itr->state == state)
				{	_trajectory.erase(trj_itr.base() - 1, _trajectory.end());
					break;
				}

			if (sar_itr + 1 != sample_trajectory.end())
				_trajectory.emplace_back(state, (*sar_itr)->action.front(), (*sar_itr)->reward[0]);
			else
				_trajectory.emplace_back(state, End_Action);
		}

		if (_trajectory.size() >= 3)
		{	causal_annotation(action_model, (*sample_trajectory.begin())->state->goal_variables(), (*sample_trajectory.begin())->state->transfer_variables());
			cleanup();
			return;
		}
	}

	throw HierException(__FILE__, __LINE__, "Input trajectory is empty.");
}


Trajectory::Trajectory (const Trajectory& trajectory, const set<int>& action_indices)
{	// Adding Start & End actions, and reconfiguring causal arcs

	if (trajectory._trajectory.empty() || action_indices.empty())
		return;

	map<int,int> new_indices;
	_trajectory.emplace_back(map<int,int>(), Start_Action);
	for (const auto& action_index : action_indices)
	{	_trajectory.push_back(trajectory._trajectory[action_index]);
		new_indices[action_index] = _trajectory.size() - 1;
	}
	_trajectory.emplace_back(trajectory._trajectory[*action_indices.rbegin() + 1].state, End_Action);

	for (unsigned node_idx = 1; node_idx < _trajectory.size() - 1; ++node_idx)
	{	// Reconfigure incoming arcs for real actions
		for (auto& arc_in : _trajectory[node_idx].causal_arcs.incoming)
		{	if (new_indices.find(arc_in.second) == new_indices.end())   // Arc points from without current sub-trajectory
			{	arc_in.second = 0;   // Arc tail at Start
				_trajectory.front().causal_arcs.outgoing[arc_in.first].insert(node_idx);
			}
			else
				arc_in.second = new_indices[arc_in.second];
		}

		// Reconfigure outgoing arcs for real actions
		for (auto& var : _trajectory[node_idx].causal_arcs.outgoing)
		{	set<int> new_outgoing_indices;
			for (const auto& arc_out : var.second)
			{	if (new_indices.find(arc_out) == new_indices.end())   // Arc points to without current sub-trajectory
				{	new_outgoing_indices.insert(_trajectory.size() - 1);   // Arc at the End action
					_trajectory.back().causal_arcs.incoming[var.first] = node_idx;
				}
				else
					new_outgoing_indices.insert(new_indices[arc_out]);
			}
			var.second = new_outgoing_indices;
		}
	}
}


void Trajectory::causal_annotation (const ActionModel& action_model, const set<int>& goal_variables, const set<int>& transfer_variables)
{	set<int> state_variables;
	if (transfer_variables.empty())
	{	for (const auto& var : _trajectory[1].state)
			state_variables.insert(var.first);
	}
	else
		state_variables = transfer_variables;

	for (const auto& var : state_variables)
	{	int tail = 0;   // Start action
		for (int head = 1; head < (int)_trajectory.size() - 1; ++head)
		{	// Check that all states are properly specified
			if (_trajectory[head].state.size() != _trajectory[1].state.size() || _trajectory[head].state.find(var) == _trajectory[head].state.end())
				throw HierException(__FILE__, __LINE__, "Malformed state in input trajectory.");

			//set<int> checked_vars = action_model.context_checked_variables(_trajectory[head].action, _trajectory[head].state);
			set<int> checked_vars = action_model.checked_variables(_trajectory[head].action);
			if (checked_vars.find(var) != checked_vars.end())
			{	_trajectory[head].causal_arcs.incoming[var] = tail;
				_trajectory[tail].causal_arcs.outgoing[var].insert(head);
			}

			if (_trajectory[head].state[var] != _trajectory[head + 1].state[var])   // Variable's value changes according to next state
				tail = head;
		}

		if (_trajectory.back().state.size() != _trajectory[1].state.size() || _trajectory.back().state.find(var) == _trajectory.back().state.end())
			throw HierException(__FILE__, __LINE__, "Malformed state in input trajectory.");

		if (goal_variables.empty() || goal_variables.find(var) != goal_variables.end())
		{	_trajectory.back().causal_arcs.incoming[var] = tail;
			_trajectory[tail].causal_arcs.outgoing[var].insert(_trajectory.size() - 1);
		}
	}
}


void Trajectory::relevance_annotation (const ActionModel& action_model)
{	set<int> state_variables;
	for (const auto& var : _trajectory[1].state)
		state_variables.insert(var.first);

	for (const auto& var : state_variables)
	{	int tail = 0;   // Start action
		for (int head = 1; head < (int)_trajectory.size(); ++head)
		{	// Check that all states are properly specified
			if (_trajectory[head].state.size() != state_variables.size() || _trajectory[head].state.find(var) == _trajectory[head].state.end())
				throw HierException(__FILE__, __LINE__, "Malformed state in input trajectory.");

			if (_trajectory[head].action != End_Action)
			{	set<int> checked_vars = action_model.context_checked_variables(_trajectory[head].action, _trajectory[head].state);
				if (checked_vars.find(var) == checked_vars.end() && _trajectory[head].state[var] == _trajectory[head + 1].state[var])
					continue;
			}
			_trajectory[head].causal_arcs.incoming[var] = tail;
			_trajectory[tail].causal_arcs.outgoing[var].insert(head);
			tail = head;
		}
	}
}


void Trajectory::cleanup ()
{	// Remove actions that do not affect the outcome
	for (unsigned a = _trajectory.size() - 2; a > 0 && _trajectory.size() > 2; --a)
		if (_trajectory[a].causal_arcs.outgoing.empty())
		{	// Remove incoming arcs
			for (const auto& in : _trajectory[a].causal_arcs.incoming)
			{	auto out_itr = _trajectory[in.second].causal_arcs.outgoing.find(in.first);
				out_itr->second.erase(a);
				if (out_itr->second.empty())
					_trajectory[in.second].causal_arcs.outgoing.erase(out_itr);
			}

			// Reindex the causal arcs to adjust for the removed action
			for (unsigned b = a + 1; b < _trajectory.size(); ++b)
				for (auto& in : _trajectory[b].causal_arcs.incoming)
				{	auto out_itr = _trajectory[in.second].causal_arcs.outgoing.find(in.first);
					out_itr->second.erase(b);
					out_itr->second.insert(b - 1);

					if (in.second > int(a))
						--in.second;
				}

			// Remove the redundant action
			_trajectory.erase(_trajectory.begin() + a);
		}
}


set<int> Trajectory::incoming_variables (const set<int>& action_indices) const
{	set<int> variables;
	for (const auto& ai : action_indices)
	{	set<int> in_vars = extract_map_keys(_trajectory[ai].causal_arcs.incoming);
		variables.insert(in_vars.begin(), in_vars.end());
	}
	return variables;
}


set<int> Trajectory::checked_variables () const
{	set<int> variables;
	for (unsigned a = 1; a < _trajectory.size(); ++a)
	{	set<int> vars = extract_map_keys(_trajectory[a].causal_arcs.incoming);
		variables.insert(vars.begin(), vars.end());
	}
	return variables;
}


set<int> Trajectory::changed_variables () const
{	set<int> variables;
	for (unsigned a = 1; a < _trajectory.size(); ++a)
	{	set<int> vars = extract_map_keys(_trajectory[a].causal_arcs.outgoing);
		variables.insert(vars.begin(), vars.end());
	}
	return variables;
}


set<int> Trajectory::outgoing_variables (const set<int>& action_indices) const
{	set<int> variables;
	for (const auto& ai : action_indices)
	{	set<int> out_vars = extract_map_keys(_trajectory[ai].causal_arcs.outgoing);
		variables.insert(out_vars.begin(), out_vars.end());
	}
	return variables;
}


set<int> Trajectory::extract_actions () const
{	set<int> actions;
	for (unsigned a = 1; a < _trajectory.size() - 1; ++a)
		actions.insert(_trajectory[a].action);
	return actions;
}


set<int> Trajectory::extract_actions (const set<int>& action_indices) const
{	set<int> actions;
	for (const auto& ai : action_indices)
		actions.insert(_trajectory[ai].action);
	return actions;
}


int Trajectory::causal_action_index (const int& action_index, const int& variable) const
{	auto var_act_itr = _trajectory[action_index].causal_arcs.incoming.find(variable);
	return var_act_itr != _trajectory[action_index].causal_arcs.incoming.end() ? var_act_itr->second : -1;
}


unique_ptr<Subtrajectory> Trajectory::goal () const
{	unique_ptr<Subtrajectory> sub_trj(new Subtrajectory);
	set<int> pre_vars = extract_map_keys(_trajectory.back().causal_arcs.incoming);
	for (const auto& var : pre_vars)
		sub_trj->preconditions[var] = _trajectory.size() - 1;
	return sub_trj;
}


unique_ptr<Subtrajectory> Trajectory::extract_subtrajectory (const set<int>& action_indices) const
{	// Absorb new actions connected via causal arcs labeled with the abstraction
	// as long as the action does not affect another outside of the current subtrajetory

	unique_ptr<Subtrajectory> sub_trj(new Subtrajectory(outgoing_variables(action_indices)));
	map<int,set<int>> search_nodes;   // action index -> set of variables to be expanded

	// Add the causal actions to the search graph
	for (const auto& ai : action_indices)
		search_nodes[ai] = incoming_variables(ai);

	bool expanded;
	do
	{	expanded = false;
		for (auto& sn : search_nodes)
		{	for (auto v_itr = sn.second.cbegin(); v_itr != sn.second.cend(); )
			{	auto causal_action_itr = _trajectory[sn.first].causal_arcs.incoming.find(*v_itr);
				if (causal_action_itr == _trajectory[sn.first].causal_arcs.incoming.end())
					throw HierException(__FILE__, __LINE__, "Unknown incoming arc.");

				if (search_nodes.find(causal_action_itr->second) == search_nodes.end())
				{	// Ignore the Start action
					if (causal_action_itr->second == 0)
					{	++v_itr;
						continue;
					}

					// Check for outgoing arcs that affect nodes beyond the search graph
					set<int> searched_actions = extract_map_keys(search_nodes);
					auto out_itr = _trajectory[causal_action_itr->second].causal_arcs.outgoing.cbegin();
					for ( ; out_itr != _trajectory[causal_action_itr->second].causal_arcs.outgoing.cend(); ++out_itr)
						if (!includes(searched_actions.begin(), searched_actions.end(), out_itr->second.begin(), out_itr->second.end()))
							break;
					if (out_itr != _trajectory[causal_action_itr->second].causal_arcs.outgoing.end())
					{	++v_itr;
						continue;
					}

					set<int> in_vars = incoming_variables(causal_action_itr->second);
					search_nodes[causal_action_itr->second] = in_vars;
					expanded = true;
				}
				sn.second.erase(v_itr++);
				if (expanded)
					break;
			}
			if (expanded)
				break;
		}
	} while (expanded);

	// Determine open preconditions
	bool search_nodes_spliced = false;
	for (auto sn_itr = search_nodes.crbegin(); sn_itr != search_nodes.crend(); ++sn_itr)
	{	for (const auto& v : sn_itr->second)
		{	map<int,int>::const_iterator op_itr = sub_trj->preconditions.find(v);
			if (op_itr == sub_trj->preconditions.end())
				sub_trj->preconditions[v] = sn_itr->first;
			else if (_trajectory[op_itr->second].causal_arcs.incoming.find(v)->second != _trajectory[sn_itr->first].causal_arcs.incoming.find(v)->second)
			{	// Variable precondition does not lead to the same action within the trajectory
				search_nodes.erase(search_nodes.begin(), sn_itr.base());   // Removing previously incorporated actions
				search_nodes_spliced = true;
				break;
			}
		}
		if (search_nodes_spliced)
			break;
	}
	if (search_nodes_spliced)   // Add the remaining incoming arcs to the preconditions
		for (const auto& in : _trajectory[search_nodes.begin()->first].causal_arcs.incoming)
			sub_trj->preconditions[in.first] = search_nodes.begin()->first;

	sub_trj->action_indices = extract_map_keys(search_nodes);   // Collect action indices

	// If seed actions have not survived the precondition check, return empty subtrajectory
	if (!includes(sub_trj->action_indices.begin(), sub_trj->action_indices.end(), action_indices.begin(), action_indices.end()))
		return nullptr;

	return sub_trj;
}


unique_ptr<Subtrajectory> Trajectory::splitter () const
{	unique_ptr<Subtrajectory> sub_trj(new Subtrajectory);
	sub_trj->action_indices.insert(_trajectory.size() - 2);

	set<int> precondition_vars = incoming_variables(*sub_trj->action_indices.begin());
	for (const auto& var : precondition_vars)
		sub_trj->preconditions[var] = *sub_trj->action_indices.begin();

	return sub_trj;
}


bool Trajectory::always_true (const Expression& exp) const
{	for (unsigned a = 1; a < _trajectory.size(); ++a)
		if (!exp.evaluate(_trajectory[a].state))
			return false;
	return true;
}


map<map<int,int>,double> Trajectory::cumulative_reward (const set<int>& action_indices) const
{	map<map<int,int>,double> state_reward;

	double reward = 0.0;
	for (auto a_itr = action_indices.crbegin(); a_itr != action_indices.crend(); ++a_itr)
	{	reward += _trajectory[*a_itr].reward;
		state_reward[_trajectory[*a_itr].state] = reward;
	}

	return state_reward;
}


void Trajectory::print_gv (const string& filename, const MDP& mdp) const
{	ofstream out((filename + ".gv").c_str());
	if (!out.is_open())
		throw HierException(__FILE__, __LINE__, "Unable to write graphviz file.");

	out << "digraph cat {\n";
	out << "rankdir=LR\n";
	out << "node [shape=plaintext]\n";
	out << "edge [arrowhead=normal]\n\n";

	// Nodes
	out << "0 [label=\"Start\",fontname=\"sans\"]\n";
	for (unsigned i = 1; i < _trajectory.size() - 1; ++i)
		out << i << " [label=\"" << mdp.print_action(_trajectory[i].action) << "\",fontname=\"sans\"]\n";
	out << _trajectory.size() - 1 << " [label=\"End\",fontname=\"sans\"]\n\n";

	// Edges
	for (unsigned i = 0; i < _trajectory.size(); ++i)
		for (const auto& out_arc : _trajectory[i].causal_arcs.outgoing)
			for (const auto& j : out_arc.second)
				out << i << " -> " << j << " [label=<<i>v</i><sub>" << out_arc.first << "</sub>>]\n";

	out << "\n# To line up the actions\n";
	for (unsigned i = 0; i < _trajectory.size() - 1; ++i)
		out << i << " -> " << i + 1 << " [weight=100,style=invis]\n";

	out << "}\n";
	out.close();

	if (system(("dot -Tpdf -o " + filename + ".pdf " + filename + ".gv").c_str()))
		cerr << "(" + to_string(__FILE__) + ":" + to_string(__LINE__) + ")\n";
}
