
/*************************************************

	CAUSALLY ANNOTATED TRAJECTORY SCANNER
		Neville Mehta

**************************************************/


#pragma once

#include <map>
#include <set>
#include <vector>
#include "../lib/graph.h"
#include "trajectory.h"


class CatScan
{	struct Subtrj_Column
	{	vector<shared_ptr<Subtrajectory>> sub_trj;   // One for each trajectory
		Expression admissibility;
		Expression termination;
		set<unsigned> column_indices;   // Column indices for which current column is a precondition

		Subtrj_Column (const unsigned& num_trajectories) : sub_trj(vector<shared_ptr<Subtrajectory>>(num_trajectories)) {}
		Subtrj_Column (const unsigned& num_trajectories, const Expression& termination)
									: sub_trj(vector<shared_ptr<Subtrajectory>>(num_trajectories)), termination(termination) {}
		Subtrj_Column (const unsigned& num_trajectories, const Expression& termination, const unsigned& column_index)
									: sub_trj(vector<shared_ptr<Subtrajectory>>(num_trajectories)), termination(termination), column_indices(make_set<unsigned>(1, column_index)) {}
	};
	vector<Subtrj_Column> subtrj_matrix;
	const vector<Trajectory>& trajectories;
	graph<int> precedence_graph;

	struct Merger
	{	int column_index;
		bool new_column;
		Merger (const int& column_index = -2, const bool& new_column = false) : column_index(column_index), new_column(new_column) {}
		bool empty () const { return column_index == -2; }
		void clear () { column_index = -2; }
		bool current () const { return column_index == -1; }
		void set_current () { column_index = -1; }
		void set (const int& col_index, const bool& new_col) { column_index = col_index; new_column = new_col; }
		bool operator == (const Merger& merge) const { return column_index == merge.column_index && new_column == merge.new_column; }
		bool operator != (const Merger& merge) const { return !(*this == merge); }
	};

	bool process();
	bool preconditions(map<int,Expression>& var_precondition) const;
	set<int> precondition_variables(const unsigned& index) const;
	void process_admissibility(const set<int>& precondition_variables);
	Merger process_merge(const vector<Merger>& merge_column) const;
	void process_termination(vector<Subtrj_Column>& st_matrix, const vector<vector<Merger>>& merger);
	bool add_column(Subtrj_Column& new_matrix_column, const vector<Merger>& merge_column);
	bool empty_column(const Subtrj_Column& new_matrix_column, const vector<Merger>& merge_column) const;
	bool subsumed_column(const Subtrj_Column& new_matrix_column, const vector<Merger>& merge_column) const;
	bool unify(vector<Subtrj_Column>& st_matrix, const set<int>& variables, vector<vector<Merger>>& merger);
	bool merge(const set<unsigned>& subtrj_column_indices);
	unsigned check_subtrajectories(const set<int>& action_indices, const int& t) const;
	unsigned check_subtrajectories(const int& action_index, const int& t) const { return check_subtrajectories(make_set<int>(1, action_index), t); }
	int check_subtrajectories(const vector<Subtrj_Column>& st_matrix, const int& action_index, const int& t) const;
	void precedence_graph_builder();
	void precedence_graph_postprocessor();
	void precedence_graph_temporal_links();
	bool causal_threat(const unsigned& task1, const unsigned& task2) const;
	void cleanup();

	public:
		CatScan (const vector<Trajectory>& trajectories, const Expression& termination);
		bool decomposed () const { return subtrj_matrix.size() > 2; }
		unsigned num_subtasks () const { return subtrj_matrix.size() - 1; }
		set<unsigned> subtask_indices (const unsigned& index) const { return precedence_graph.parents(index, 0); }   // Precondition subtrajectories are child tasks
		set<int> abstraction(const unsigned& index, const ActionModel& action_model) const;
		vector<Trajectory> subtrajectories(const unsigned& index) const;
		Expression admissibility (const unsigned& index) const { return subtrj_matrix[index].admissibility; }
		Expression termination (const unsigned& index) const { return subtrj_matrix[index].termination; }
		~CatScan () {}
};
