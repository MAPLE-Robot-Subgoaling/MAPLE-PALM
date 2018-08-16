
/**********************************************************************************

	GENERIC COMPOSITE TASK
		Neville Mehta

***********************************************************************************/


#pragma once


#include <string>
#include <vector>
#include "../../../lib/matrix.h"
#include "../../../hiergen/expression.h"
#include "task.h"


namespace composite_task_parameters {
const double EPSILON = 0.08;   // Exploration factor
}


class CompositeTask : public virtual Task
{	struct Parameter
	{	int type;
		int value;
		/* The 'type' field indicates the context of the 'value' field:
		    type = 0: value contains the actual binding value;
		    type = 1: value indexes the parent's parameter vector;
		    type = 2: value indexes the state variable. */
		int offset;   // Offsets the net value
	};

	struct SubTask   // Local data structure
	{	Task* link;
		vector<Parameter> parameters;

		SubTask (Task* link, const vector<Parameter> parameters) : link(link), parameters(parameters) {}
	};

	protected:
		unsigned _num_subtasks;   // Number of subtasks
		vector<SubTask> _subtask;   // Vector of pointers to subtasks with their associated bindings
		bool (*_termination_function)(const unsigned& agent, const vector<int>& parameters, const State& state);
		Expression _termination_expression;

		vector<unsigned> admissible_subtasks(const unsigned& agent, const vector<int>& parameters, const State& state) const;

	public:
		struct Subtask   // Input format
		{	Task* link;
			string parameters;

			Subtask (Task* T = nullptr, const string& parameters = "") : link(T), parameters(parameters) {}
			bool operator == (const Subtask& subtask) const { return link == subtask.link && parameters == subtask.parameters; }
		};

		CompositeTask(const string& task_name, const string& parameters, const string& state_variables,
					const vector<Subtask>& subtasks, bool (*function)(const unsigned& agent, const vector<int>& parameters, const State& state), const Expression& expression, const MDP& mdp);
		bool primitive () const { return false; }
		bool admissible (const unsigned& agent, const vector<int>& parameters, const State& state) const { return !terminated(agent, parameters, state); }
		unsigned num_subtasks () const { return _num_subtasks; }
		Task* get_subtask (const unsigned& t) const { return _subtask[t].link; }
		vector<int> subtask_bindings(const unsigned& agent, const vector<int>& parameters, const State& state, const unsigned& t) const;
		string binding_str(const unsigned& t) const;
		string binding_str_html(const unsigned& t) const;
		virtual void add_subtask (const Subtask& subtask, const MDP& mdp);
		virtual unsigned greedy_policy(const unsigned& agent, const vector<int>& parameters, const State& state) = 0;
		virtual unsigned exploratory_policy(const unsigned& agent, const vector<int>& parameters, const State& state);
		virtual bool terminated(const unsigned& agent, const vector<int>& parameters, const State& state) const;
		Expression termination_expression () const { return _termination_expression; }
		virtual string print_dot() const;
		virtual ~CompositeTask() {}
};


class ParameterException : public HierException
{	public:
		ParameterException (const string& filename, const int& line, const string& error) : HierException(filename, line, error) {}
		virtual ~ParameterException () throw() {}
};
