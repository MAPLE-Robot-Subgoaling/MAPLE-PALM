
/**********************************************************************************

	GENERIC TASK FRAMEWORK
		Neville Mehta

***********************************************************************************/


#pragma once


#include <list>
#include <memory>
#include <string>
#include <vector>
#include "../../../domain/mdp.h"


class Task
{	protected:
		struct Variable
		{	int type;   // Indicates the context of the 'value' field: type = 0: value indexes the state variable; type >= 1: value indexes the parameter-based state variable
			int value;

			Variable (const int& type, const int& value) : type(type), value(value) {}
		};

		const string _name;   // Name of task
		unsigned _num_bindings;   // Total number of parameter binding combinations
		list<Variable> _variables;   // Task's state variables
		unsigned _num_states;   // Number of states

	public:
		vector<int> _parameter_size;   // Task parameters

		Task(const string& task_name, const string& parameters, const string& state_variables, const MDP& mdp);
		string name () const { return _name; }
		virtual bool primitive() const = 0;
		virtual bool admissible(const unsigned& agent, const vector<int>& parameters, const State& state) const = 0;
		virtual void initialize() = 0;
		set<int> abstraction() const;
		string abstraction_str() const;
		string abstraction_str_html() const;
		int hash(const unsigned& agent, const vector<int>& parameters, const State& state) const;
		pair<vector<int>, unique_ptr<State>> unhash(const unsigned& agent, int hash, const State& state) const;
		unsigned num_parameters () const { return _parameter_size.size(); }
		virtual bool update_parameters(const vector<int>& parameter_size);
		virtual bool terminated(const unsigned& agent, const vector<int>& parameters, const State& state) const = 0;
		virtual string print(const State& state) const = 0;
		virtual string print_dot() const;   // Graphviz dot format
		virtual ~Task () {}
};
