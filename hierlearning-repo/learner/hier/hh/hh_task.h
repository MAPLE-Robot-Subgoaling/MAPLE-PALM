
/***********************************************************************

   	HIERARCHICAL H TASK
		Neville Mehta

************************************************************************/


#pragma once


#include <string>
#include <valarray>
#include "../../../domain/mdp.h"
#include "../generic/task.h"


class HH_Task : public virtual Task
{	protected:
		valarray<double> _H;   // H(s) table (it doubles as the Total_reward(s) table)
		valarray<double> _Time;   // Time(s) table;

	public:
		HH_Task(const string& task_name, const string& parameters, const string& state_variables, const MDP& mdp);
		virtual void initialize();
		double h (const int& s) const { return _H[s]; }
		double time (const int& s) const { return _Time[s]; }
		virtual ~HH_Task() {}
};
