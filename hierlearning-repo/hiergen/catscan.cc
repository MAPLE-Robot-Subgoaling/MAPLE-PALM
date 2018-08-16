
/*************************************************

	CAUSALLY ANNOTATED TRAJECTORY SCANNER
		Neville Mehta

**************************************************/


#include <algorithm>
#include <fstream>
#include <iterator>
#include "../lib/common.h"
#include "catscan.h"



CatScan::CatScan (const vector<Trajectory>& trajectories, const Expression& termination) : trajectories(trajectories)
{	// Add the preconditions of End action
	subtrj_matrix.emplace_back(trajectories.size(), termination);
	for (unsigned t = 0; t < trajectories.size(); ++t)
		subtrj_matrix.back().sub_trj[t] = trajectories[t].goal();
	process();
	cleanup();

	// Trajectory split
	if (!decomposed())   // Only Root or with single child
	{	if (subtrj_matrix.size() == 2)
			subtrj_matrix.pop_back();   // Remove single child

		subtrj_matrix.emplace_back(trajectories.size(), Expression(), 0);
		bool successful_split = true;
		for (unsigned t = 0; t < trajectories.size(); ++t)
		{	subtrj_matrix.back().sub_trj[t] = trajectories[t].splitter();
			if (!subtrj_matrix.back().sub_trj[t])
			{	successful_split = false;
				break;
			}
		}

		if (successful_split)
		{	process();
			cleanup();
		}
		else
			subtrj_matrix.pop_back();
	}

	if (decomposed())
		precedence_graph_builder();
}


bool CatScan::process ()
{	map<int,Expression> var_precondition;   // variable -> precondition
	if (preconditions(var_precondition))
		return true;
	if (var_precondition.empty())
		return false;

	// Admissibility restricted to the abstraction variables
	process_admissibility(extract_map_keys(var_precondition));

	// Process preconditions
	vector<Subtrj_Column> new_subtrj_matrix;
	vector<vector<Merger>> merger;
	for (const auto& var_prec : var_precondition)
	{	new_subtrj_matrix.emplace_back(trajectories.size(), var_prec.second, subtrj_matrix.size() - 1);
		merger.emplace_back(trajectories.size());
		for (unsigned t = 0; t < trajectories.size(); ++t)
			if (subtrj_matrix.back().sub_trj[t])
			{	map<int,int>::const_iterator p_itr = subtrj_matrix.back().sub_trj[t]->preconditions.find(var_prec.first);
				if (p_itr != subtrj_matrix.back().sub_trj[t]->preconditions.end())
				{	int causal_action_index = trajectories[t].causal_action_index(p_itr->second, var_prec.first);
					if (causal_action_index > 0)
					{	int subtrj_index;
						if ((subtrj_index = check_subtrajectories(causal_action_index, t)))   // Subtrajectory match
							merger.back()[t].set(subtrj_index, false);
						else if ((subtrj_index = check_subtrajectories(new_subtrj_matrix, causal_action_index, t)) != -1)
						{	new_subtrj_matrix.back().sub_trj[t] = move(new_subtrj_matrix[subtrj_index].sub_trj[t]);
							merger.back()[t].set(subtrj_index, true);
						}
						else
						{	new_subtrj_matrix.back().sub_trj[t] = trajectories[t].extract_subtrajectory(causal_action_index);
							merger.back()[t].set_current();
						}
					}
				}
			}
	}
	process_termination(new_subtrj_matrix, merger);

	const vector<Subtrj_Column> original_subtrj_matrix(subtrj_matrix);
	set<unsigned> bad_subtrj_columns;
	if (unify(new_subtrj_matrix, extract_map_keys(var_precondition), merger))
	{	for (unsigned k = 0; k < new_subtrj_matrix.size(); ++k)
			if (add_column(new_subtrj_matrix[k], merger[k]) && !process())
				bad_subtrj_columns.insert(subtrj_matrix.size() - 1);
	}
	else
	{	for (const auto& nsm : new_subtrj_matrix)
		{	subtrj_matrix.push_back(nsm);
			bad_subtrj_columns.insert(subtrj_matrix.size() - 1);
		}
	}

	if (bad_subtrj_columns.empty() || (merge(bad_subtrj_columns) && process()))
		return true;

	// Reset the matrix
	subtrj_matrix = original_subtrj_matrix;
	return false;
}


bool CatScan::preconditions (map<int,Expression>& var_precondition) const
{	bool empty_states = true;
	set<int> pre_vars = precondition_variables(subtrj_matrix.size() - 1);
	for (const auto& var : pre_vars)
	{	vector<map<int,int>> states;
		for (unsigned t = 0; t < trajectories.size(); ++t)
			if (subtrj_matrix.back().sub_trj[t])
			{	auto p_itr = subtrj_matrix.back().sub_trj[t]->preconditions.find(var);
				if (p_itr != subtrj_matrix.back().sub_trj[t]->preconditions.cend())
				{	int causal_action_index = trajectories[t].causal_action_index(p_itr->second, p_itr->first);
					if (causal_action_index > 0)   // If the arc exists and does not come from the start state
						states.push_back(trajectories[t].extract_state(causal_action_index + 1));
				}
			}

		if (!states.empty())
		{	empty_states = false;
			Expression exp = Expression(states, var);
			if (!exp.empty())
			{	// Ignore conditions that are always true in every trajectory or the same as the overall termination
				if (exp == subtrj_matrix[0].termination)
					exp.clear();
				else
				{	unsigned t;
					for (t = 0; t < trajectories.size(); ++t)
						if (!trajectories[t].always_true(exp))
							break;
					if (t == trajectories.size())
						exp.clear();
				}
			}
			if (exp.empty())
			{	var_precondition.clear();
				return false;
			}
			var_precondition[var] = exp;
		}
	}

	return empty_states;
}


set<int> CatScan::precondition_variables (const unsigned& index) const
{	set<int> all_pre_vars;
	for (const auto& st : subtrj_matrix[index].sub_trj)
		if (st)
		{	set<int> pre_vars = extract_map_keys(st->preconditions);
			all_pre_vars.insert(pre_vars.begin(), pre_vars.end());
		}
	return all_pre_vars;
}


void CatScan::process_admissibility (const set<int>& precondition_variables)
{	// Admissibility restricted to the precondition variables
	vector<map<int,int>> states;
	for (unsigned t = 0; t < trajectories.size(); ++t)
		if (subtrj_matrix.back().sub_trj[t] && !subtrj_matrix.back().sub_trj[t]->action_indices.empty())
			states.push_back(trajectories[t].extract_state(*subtrj_matrix.back().sub_trj[t]->action_indices.begin()));
	if (!states.empty())
		subtrj_matrix.back().admissibility = Expression(states, precondition_variables);
}


CatScan::Merger CatScan::process_merge (const vector<Merger>& merge_column) const
{	Merger merge;
	for (const auto& mc : merge_column)
	{	if (!mc.empty() && merge.empty())
		{	merge = mc;
			continue;
		}
		if (!mc.empty() && mc != merge)
			return Merger();
	}
	return merge;
}


void CatScan::process_termination (vector<Subtrj_Column>& st_matrix, const vector<vector<Merger>>& merger)
{	for (unsigned c = 0; c < merger.size(); ++c)
	{	Merger merge = process_merge(merger[c]);
		if (!merge.empty() && !merge.current() && merge.new_column)
			st_matrix[c].termination.conjunct(st_matrix[merge.column_index].termination);
	}
}


bool CatScan::add_column (Subtrj_Column& new_matrix_column, const vector<Merger>& merge_column)
{	if (empty_column(new_matrix_column, merge_column))
		return false;

	if (subsumed_column(new_matrix_column, merge_column))
		return false;

	Merger merge = process_merge(merge_column);
	if (!merge.empty() && !merge.current() && !merge.new_column)
	{	new_matrix_column.termination.conjunct(subtrj_matrix[merge.column_index].termination);
		new_matrix_column.column_indices.insert(subtrj_matrix[merge.column_index].column_indices.begin(), subtrj_matrix[merge.column_index].column_indices.end());
	}

	for (unsigned t = 0; t < new_matrix_column.sub_trj.size(); ++t)
	{	if (!merge_column[t].new_column && !merge_column[t].empty() && !merge_column[t].current())
			new_matrix_column.sub_trj[t] = move(subtrj_matrix[merge_column[t].column_index].sub_trj[t]);
		else if (new_matrix_column.sub_trj[t])
		{	unsigned subtrj_column = check_subtrajectories(new_matrix_column.sub_trj[t]->action_indices, t);
			if (subtrj_column)
				subtrj_matrix[subtrj_column].sub_trj[t] = nullptr;
		}
	}
	subtrj_matrix.push_back(new_matrix_column);
	return true;
}


bool CatScan::empty_column (const Subtrj_Column& new_matrix_column, const vector<Merger>& merge_column) const
{	bool is_empty_column = true;
	Merger merge;
	unsigned t;
	for (t = 0; t < merge_column.size(); ++t)
	{	if (new_matrix_column.sub_trj[t])
			is_empty_column = false;

		if (!merge_column[t].empty() && merge.empty())
		{	merge = merge_column[t];
			continue;
		}
		if (!merge_column[t].empty() && merge_column[t] != merge)
			break;
	}
	if (t == merge_column.size() && is_empty_column && (merge.empty() || merge.current() || merge.new_column))
		return true;
	return false;
}


bool CatScan::subsumed_column (const Subtrj_Column& new_matrix_column, const vector<Merger>& merge_column) const
{	Merger merge;
	unsigned subtrj_column = 0;
	for (const auto& mc : merge_column)
	{	if (!mc.empty() && merge.empty())
		{	merge = mc;
			if (merge.new_column)
				return false;
			continue;
		}
		if (!mc.empty() && mc != merge)
			return false;
	}

	if (!merge.empty() && !merge.current())
		subtrj_column = merge.column_index;
	else
	{	for (unsigned t = 0; t < new_matrix_column.sub_trj.size(); ++t)
			if (new_matrix_column.sub_trj[t])
			{	if (!subtrj_column)
				{	subtrj_column = check_subtrajectories(new_matrix_column.sub_trj[t]->action_indices, t);
					if (!subtrj_column)
						return false;
					continue;
				}
				if (subtrj_column != check_subtrajectories(new_matrix_column.sub_trj[t]->action_indices, t))
					return false;
			}
	}

	// Check strict subsumption
	for (unsigned t = 0; t < subtrj_matrix[subtrj_column].sub_trj.size(); ++t)
		if (subtrj_matrix[subtrj_column].sub_trj[t] && merge_column[t].empty() && !new_matrix_column.sub_trj[t])
			return true;

	return false;
}


bool CatScan::unify (vector<Subtrj_Column>& st_matrix, const set<int>& variables, vector<vector<Merger>>& merger)
{	// If two state variables coexist in any subset of a factoring (partition of the variables), then they are in the same subset of the final partition

	if (st_matrix.size() < 2)
		return true;

	// Determine global factoring
	vector<set<int>> global_factors;
	vector<vector<set<int>>> local_factors;
	for (unsigned t = 0; t < trajectories.size(); ++t)
	{	local_factors.push_back(vector<set<int>>());
		for (const auto& stm : st_matrix)
		{	set<int> factor;
			if (stm.sub_trj[t])
				set_intersection(stm.sub_trj[t]->outgoing_variables.begin(), stm.sub_trj[t]->outgoing_variables.end(), variables.begin(), variables.end(), inserter(factor, factor.end()));
			local_factors.back().push_back(factor);
		}

		if (global_factors.empty())
			global_factors = local_factors.back();
		else
		{	for (unsigned k = 0; k < st_matrix.size(); ++k)
				global_factors[k].insert(local_factors.back()[k].begin(), local_factors.back()[k].end());
		}
	}

	// Collate global factors toward the end
	// Each column of the matrix is generated for a particular variable in the preconditions;
	// however, later columns might absorb subtrajectories associated with earlier columns
	for (unsigned gf = 0; gf < global_factors.size() - 1; ++gf)
		if (!global_factors[gf].empty())
		{	set<int> new_factor(global_factors[gf]);
			global_factors[gf].clear();
			unsigned fi = gf;
			for (unsigned f = gf + 1; f < global_factors.size(); ++f)
			{	set<int> result;
				set_intersection(new_factor.begin(), new_factor.end(), global_factors[f].begin(), global_factors[f].end(), inserter(result, result.end()));
				if (!result.empty())
				{	new_factor.insert(global_factors[f].begin(), global_factors[f].end());
					global_factors[f].clear();
					fi = f;
				}
			}
			global_factors[fi] = new_factor;
		}

	// Readjust the subtrajectories based on the global factors
	const vector<Subtrj_Column> matrix_reset(st_matrix);   // Revert to original after failure
	const vector<vector<Merger>> merger_reset(merger);
	for (unsigned t = 0; t < trajectories.size(); ++t)
	{	// Reconcile local factors with the global factoring by merging subtrajectories
		for (unsigned f = 0; f < global_factors.size(); ++f)
			if (!global_factors[f].empty())
			{	set<int> action_indices;
				unsigned nonempty_subtrj = 0;
				set<unsigned> cleaner;
				for (unsigned lf = 0; lf <= f; ++lf)
				{	if (!local_factors[t][lf].empty() && includes(global_factors[f].begin(), global_factors[f].end(), local_factors[t][lf].begin(), local_factors[t][lf].end()))
					{	action_indices.insert(*st_matrix[lf].sub_trj[t]->action_indices.rbegin());
						st_matrix[f].termination.conjunct(st_matrix[lf].termination);
						nonempty_subtrj = lf;
						cleaner.insert(lf);
					}
				}
				if (!action_indices.empty())   // Refactor when local factoring != global factoring
				{	if (action_indices.size() == 1)
					{	if (nonempty_subtrj != f)
						{	st_matrix[f].sub_trj[t] = move(st_matrix[nonempty_subtrj].sub_trj[t]);
							merger[nonempty_subtrj][t].clear();
							merger[f][t].set_current();
						}
					}
					else
					{	for (auto c_itr = cleaner.cbegin(); c_itr != cleaner.cend(); ++c_itr)
						{	st_matrix[*c_itr].sub_trj[t] = nullptr;
							merger[*c_itr][t].clear();
						}

						st_matrix[f].sub_trj[t] = trajectories[t].extract_subtrajectory(action_indices);

						if (st_matrix[f].sub_trj[t])
							merger[f][t].set_current();
						else   // Unification failure
						{	st_matrix = matrix_reset;
							merger = merger_reset;
							return false;
						}
					}
				}
			}
	}

	return true;
}


bool CatScan::merge (const set<unsigned>& subtrj_column_indices)
{	// Merge invalid subtrajectories together

	if (subtrj_column_indices.size() == 1)
		return false;

	const vector<Subtrj_Column> matrix_reset(subtrj_matrix);   // Revert to original after failure

	subtrj_matrix.emplace_back(trajectories.size());
	for (const auto& sci : subtrj_column_indices)
	{	subtrj_matrix.back().termination.conjunct(subtrj_matrix[sci].termination);
		subtrj_matrix.back().column_indices.insert(subtrj_matrix[sci].column_indices.begin(), subtrj_matrix[sci].column_indices.end());
	}

	for (unsigned t = 0; t < trajectories.size(); ++t)
	{	unsigned nonempty_subtrj = 0;
		set<int> new_action_indices;
		set<unsigned> cleaner;
		for (const auto& sci : subtrj_column_indices)
			if (subtrj_matrix[sci].sub_trj[t])
			{	new_action_indices.insert(*subtrj_matrix[sci].sub_trj[t]->action_indices.rbegin());
				cleaner.insert(sci);
				nonempty_subtrj = sci;
			}

		if (!new_action_indices.empty())
		{	if (new_action_indices.size() == 1)
				subtrj_matrix.back().sub_trj[t] = move(subtrj_matrix[nonempty_subtrj].sub_trj[t]);
			else
			{	for (const auto& c : cleaner)
					subtrj_matrix[c].sub_trj[t] = nullptr;

				subtrj_matrix.back().sub_trj[t] = trajectories[t].extract_subtrajectory(new_action_indices);

				if (!subtrj_matrix.back().sub_trj[t])   // Merge failure
				{	subtrj_matrix = matrix_reset;
					return false;
				}

				// Absorb any subsumed subtrajectories
/*				for (unsigned k = 1; k < subtrj_matrix.size() - 1; ++k)
				{	if (subtrj_matrix[k].sub_trj[t] && includes(subtrj_matrix.back().sub_trj[t]->action_indices.begin(), subtrj_matrix.back().sub_trj[t]->action_indices.end(),
									subtrj_matrix[k].sub_trj[t]->action_indices.begin(), subtrj_matrix[k].sub_trj[t]->action_indices.end()))
						subtrj_matrix[k].sub_trj[t] = nullptr;
				}
*/			}
		}
	}

	// Erase original columns
	unsigned erase_offset = 0;
	for (auto sci_itr = subtrj_column_indices.cbegin(); sci_itr != subtrj_column_indices.cend(); ++sci_itr, ++erase_offset)
	{	subtrj_matrix.erase(subtrj_matrix.begin() + (*sci_itr - erase_offset));

		// Adjust subtrj column indices
		for (unsigned c = 1; c < subtrj_matrix.size(); ++c)
			for (auto ci_itr = subtrj_matrix[c].column_indices.cbegin(); ci_itr != subtrj_matrix[c].column_indices.cend(); )
			{	if (*ci_itr > *sci_itr - erase_offset)
				{	subtrj_matrix[c].column_indices.insert(*ci_itr - 1);
					subtrj_matrix[c].column_indices.erase(ci_itr++);
				}
				else
					++ci_itr;
			}
	}

	return true;
}


set<int> CatScan::abstraction (const unsigned& index, const ActionModel& action_model) const
{	set<int> actions;
	set<int> goal_variables;
	for (unsigned t = 0; t < subtrj_matrix[index].sub_trj.size(); ++t)
		if (subtrj_matrix[index].sub_trj[t])
		{	// Actions
			set<int> trajectory_actions = trajectories[t].extract_actions(subtrj_matrix[index].sub_trj[t]->action_indices);
			actions.insert(trajectory_actions.begin(), trajectory_actions.end());

			// Goal variables
			goal_variables.insert(subtrj_matrix[index].sub_trj[t]->outgoing_variables.begin(), subtrj_matrix[index].sub_trj[t]->outgoing_variables.end());
		}

	return action_model.closure(actions, goal_variables);
}


vector<Trajectory> CatScan::subtrajectories (const unsigned& index) const
{	vector<Trajectory> subtrajectories;
	for (unsigned t = 0; t < trajectories.size(); ++t)
		if (subtrj_matrix[index].sub_trj[t] && !subtrj_matrix[index].sub_trj[t]->action_indices.empty())
			subtrajectories.emplace_back(trajectories[t], subtrj_matrix[index].sub_trj[t]->action_indices);
	return subtrajectories;
}


unsigned CatScan::check_subtrajectories (const set<int>& action_indices, const int& t) const
{	// Check if the set of causal actions has been expanded before
	for (unsigned k = 1; k < subtrj_matrix.size(); ++k)
		if (subtrj_matrix[k].sub_trj[t] && includes(subtrj_matrix[k].sub_trj[t]->action_indices.begin(), subtrj_matrix[k].sub_trj[t]->action_indices.end(), action_indices.begin(), action_indices.end()))
			return k;

	return 0;   // Not found
}


int CatScan::check_subtrajectories (const vector<Subtrj_Column>& st_matrix, const int& action_index, const int& t) const
{	// Check if the set of causal actions has been expanded before
	for (unsigned k = 0; k < st_matrix.size() - 1; ++k)
		if (st_matrix[k].sub_trj[t] && st_matrix[k].sub_trj[t]->action_indices.find(action_index) != st_matrix[k].sub_trj[t]->action_indices.end())
			return k;

	return -1;   // Not found
}


void CatScan::precedence_graph_builder ()
{	precedence_graph.resize(subtrj_matrix.size());
	for (unsigned c = 0; c < subtrj_matrix.size(); ++c)
		for (const auto& ci : subtrj_matrix[c].column_indices)
			precedence_graph.set_edge(c, ci, 0);   // 0 = causal edge

	if (!precedence_graph.is_acyclic())
		throw HierException(__FILE__, __LINE__, "Precedence graph is not acyclic.");

	precedence_graph_postprocessor();
	//precedence_graph_temporal_links();

	//precedence_graph.print_gv("precedence_graph.gv");
	//if (system("dot -Tpdf -o precedence_graph.pdf precedence_graph.gv"))
	//	cerr << "(" + to_string(__FILE__) + ":" + to_string(__LINE__) + ")\n";
}


void CatScan::precedence_graph_postprocessor ()
{	precedence_graph.transitive_reduction(0);

	// If the root task has a single parent, then relocate grandparents to root (and leave newly orphaned parent)
	set<unsigned> root_parents = precedence_graph.parents(0, 0);
	if (root_parents.size() == 1)
	{	unsigned root_parent = *root_parents.begin();
		set<unsigned> new_parents = precedence_graph.parents(root_parent, 0);
		for (const auto& np : new_parents)
		{	precedence_graph.set_edge(np, 0, 0);
			precedence_graph.remove_edge(np, root_parent, 0);
		}
		subtrj_matrix[root_parent].termination.clear();   // For the hierarchy builder to know that the subtrajectories are associated with the parent
	}
}


void CatScan::precedence_graph_temporal_links ()
{	// Protect causal links among unordered precondition tasks
	for (unsigned i = 0; i < precedence_graph.num_vertices(); ++i)
	{	set<unsigned> precondition_tasks = precedence_graph.parents(i);
		for (auto pt_itr1 = precondition_tasks.cbegin(); pt_itr1 != precondition_tasks.cend(); ++pt_itr1)
		{	auto pt_itr2 = pt_itr1;
			++pt_itr2;
			for ( ; pt_itr2 != precondition_tasks.cend(); ++pt_itr2)
				if (*pt_itr1 != *pt_itr2 && precedence_graph.shortest_path(*pt_itr1, *pt_itr2).empty() && precedence_graph.shortest_path(*pt_itr2, *pt_itr1).empty())
				{	// Add temporal edge
					if (causal_threat(*pt_itr1, *pt_itr2))
						precedence_graph.set_edge(*pt_itr1, *pt_itr2, 1);
					if (causal_threat(*pt_itr2, *pt_itr1))
						precedence_graph.set_edge(*pt_itr2, *pt_itr1, 1);
				}
		}
	}
}


bool CatScan::causal_threat (const unsigned& task1, const unsigned& task2) const
{	// Check if task1's preconditions are clobbered by task2

	set<int> task1_vars = subtrj_matrix[task1].admissibility.lhs_variables();
	set<int> task2_vars = subtrj_matrix[task2].termination.lhs_variables();
	set<int> result;
	set_intersection(task1_vars.begin(), task1_vars.end(), task2_vars.begin(), task2_vars.end(), inserter(result, result.end()));
	return !result.empty();
}


void CatScan::cleanup ()
{	// Remove any fully empty columns
	for (unsigned k = 1; k < subtrj_matrix.size(); )
	{	unsigned t;
		for (t = 0; t < subtrj_matrix[k].sub_trj.size(); ++t)
			if (subtrj_matrix[k].sub_trj[t])
				break;
		if (t == subtrj_matrix[k].sub_trj.size())
		{	subtrj_matrix.erase(subtrj_matrix.begin() + k);

			// Adjust subtrj column indices
			for (unsigned c = 1; c < subtrj_matrix.size(); ++c)
				for (auto ci_itr = subtrj_matrix[c].column_indices.cbegin(); ci_itr != subtrj_matrix[c].column_indices.cend(); )
				{	if (*ci_itr >= k)
					{	if (*ci_itr > k)
							subtrj_matrix[c].column_indices.insert(*ci_itr - 1);
						subtrj_matrix[c].column_indices.erase(ci_itr++);
					}
					else
						++ci_itr;
				}
		}
		else
			++k;
	}
}
