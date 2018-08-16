
/**********************************************************************************

	MAXQ TASK FRAMEWORK
		Neville Mehta

***********************************************************************************/


#pragma once


#include <list>
#include <string>
#include <vector>
#include "../../../domain/mdp.h"
#include "../generic/task.h"



class MaxQ_Task : public virtual Task
{	public:
		MaxQ_Task(const string& task_name, const string& parameters, const string& state_variables, const MDP& mdp) : Task(task_name, parameters, state_variables, mdp) {}
		virtual double V(const unsigned& agent, const vector<int>& parameters, const State& state) const = 0;
		virtual ~MaxQ_Task () {}
};
