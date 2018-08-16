
/***********************************************************************

   	HIERARCHICAL H TASK
		Neville Mehta

************************************************************************/


#include "hh_task.h"



HH_Task::HH_Task (const string& task_name, const string& parameters, const string& state_variables, const MDP& mdp)	: Task(task_name, parameters, state_variables, mdp)
{	// Create the H(s) and Time(s) tables
	_H.resize(_num_bindings * _num_states);
	_Time.resize(_num_bindings * _num_states);
}


void HH_Task::initialize ()
{	// Initializing the H(s) and Time(s) tables
	_H = 0.0;
	_Time = 0.0;
}
