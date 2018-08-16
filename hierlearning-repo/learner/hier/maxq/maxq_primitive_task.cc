
/**********************************************************************************

	MAXQ PRIMITIVE TASK
		Neville Mehta

***********************************************************************************/


#include <sstream>
#include "../../../lib/common.h"
#include "maxq_primitive_task.h"



MaxQ_PrimTask::MaxQ_PrimTask (const string& task_name, const string& state_variables, const int& action, const MDP& mdp, const Value_function_type v_type)
				: Task(task_name, "", state_variables, mdp), PrimTask(task_name, state_variables, action, mdp), MaxQ_Task(task_name, "", state_variables, mdp), _v_type(v_type)
{	if (_v_type == Tabular)
		_V.resize(_num_states);
	else if (_v_type == Linear)
		_weight.resize(_variables.size() + 1);
}


void MaxQ_PrimTask::initialize ()
{	if (_v_type == Tabular)
		_V = 0.0;
	else if (_v_type == Linear)
		_weight = 0.0;
}


inline valarray<double> MaxQ_PrimTask::feature_vector (const unsigned& agent, const State& state) const
{	valarray<double> feature(_variables.size() + 1);

	// State variables
	unsigned f = 0;
	for (const auto& v : _variables)
		switch (v.type)
		{	case 0:
				feature[f++] = state.variable(v.value);
				break;

			default:
				feature[f++] = state.variable(agent * state.num_agent_variables() + v.value);
				break;
		}
	feature[f] = 1.0;

	return feature;
}


double MaxQ_PrimTask::V (const unsigned& agent, const vector<int>& parameters, const State& state) const
{	if (_v_type == Tabular)
		return _V[hash(agent, vector<int>(), state)];
	else if (_v_type == Linear)
		return (_weight * feature_vector(agent, state)).sum();
	else
		return 0.0;
}


void MaxQ_PrimTask::update (const unsigned& agent, const State& state, const double& reward)
{	if (_v_type == Tabular)
	{	int s = hash(agent, vector<int>(), state);
		_V[s] += maxq_primitive_task::TABULAR_ALPHA * (reward - _V[s]);
	}
	else if (_v_type == Linear)
		_weight += maxq_primitive_task::LINEAR_ALPHA * (reward - V(agent, vector<int>(), state)) * feature_vector(agent, state);
}


string MaxQ_PrimTask::print (const State& state) const
{	ostringstream out;
	out << "\n\n******** " << _name << " ********\n";
	if (_v_type == Tabular)
	{	out << "\nV(s):\n";
		for (unsigned s = 0; s < _num_states; ++s)
			if (_V[s] != 0.0)
				out << "[" << unhash(0, s, state).second->print() << "]   " << _V[s] << endl;
	}
	else if (_v_type == Linear)
		out << "\nWeights: " << _weight << endl;
	return out.str();
}
