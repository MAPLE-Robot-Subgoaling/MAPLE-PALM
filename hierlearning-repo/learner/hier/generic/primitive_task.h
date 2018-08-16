
/**********************************************************************************

	GENERIC PRIMITIVE TASK
		Neville Mehta

***********************************************************************************/


#pragma once


#include <string>
#include "task.h"



class PrimTask : public virtual Task
{	protected:
		const int _primitive_action;

	public:
		PrimTask(const string& task_name, const string& state_variables, const int& action, const MDP& mdp)
														: Task(task_name, "", state_variables, mdp), _primitive_action(action) {}
		bool primitive () const { return true; }
		int primitive_action () const { return _primitive_action; }
		bool admissible (const unsigned& agent, const vector<int>& parameters, const State& state) const { return true; }
		bool terminated (const unsigned& agent, const vector<int>& parameters, const State& state) const { return true; }
		virtual string print_dot() const { return Task::print_dot() + "{" + abstraction_str_html() + "}>,shape=ellipse]\n"; }
		virtual ~PrimTask() {}
};
