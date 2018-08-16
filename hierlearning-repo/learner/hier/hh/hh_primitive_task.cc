
/***********************************************************************

   	HIERARCHICAL H PRIMITIVE TASK
		Neville Mehta

************************************************************************/


#include <sstream>
#include "hh_primitive_task.h"



HH_PrimTask::HH_PrimTask (const string& task_name, const string& state_variables, const int& action, const MDP& mdp)
				: Task(task_name, "", state_variables, mdp), PrimTask(task_name, state_variables, action, mdp), HH_Task(task_name, "", state_variables, mdp)
{	_N.resize(_num_states);
}


void HH_PrimTask::initialize ()
{	HH_Task::initialize();
	_N = 0;
}


void HH_PrimTask::update (const unsigned& agent, const State& state, const double& reward, const double& elapsed_time)
{	int s = hash(agent, vector<int>(), state);
	_N[s]++;
	_Time[s] += (elapsed_time - _Time[s]) / _N[s];
	_H[s] += (reward - _H[s]) / _N[s];
}


string HH_PrimTask::print (const State& state) const
{	ostringstream out;
	out << "\n\n******** " << _name << " ********\n";

	out << "\nReward(s):\n";
	for (unsigned s = 0; s < _num_states; s++)
		if (_H[s] != 0.0)
			out << "[" << unhash(0, s, state).second->print() << "]   " << _H[s] << endl;

	out << "\nTime(s):\n";
	for (unsigned s = 0; s < _num_states; s++)
		if (_Time[s] != 0.0)
			out << "[" << unhash(0, s, state).second->print() << "]   " << _Time[s] << endl;

	out << "\nN(s):\n";
	for (unsigned s = 0; s < _num_states; s++)
		if (_N[s] > 0)
			out << "[" << unhash(0, s, state).second->print() << "]   " << _N[s] << endl;

	return out.str();
}
