
/***********************************************************************

   	HIERARCHICAL H PRIMITIVE TASK
		Neville Mehta

************************************************************************/


#pragma once

#include <string>
#include "../generic/primitive_task.h"
#include "hh_task.h"


class HH_PrimTask : public PrimTask, public HH_Task
{	protected:
		valarray<int> _N;

	public:
		HH_PrimTask(const string& task_name, const string& state_variables, const int& action, const MDP& mdp);
		void initialize();
		void update(const unsigned& agent, const State& state, const double& reward, const double& elapsed_time);
		string print(const State& state) const;
		~HH_PrimTask() {}
};
