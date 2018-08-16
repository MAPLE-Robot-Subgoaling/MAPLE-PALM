
/**********************************************************************************

	MAXQ PRIMITIVE TASK
		Neville Mehta

***********************************************************************************/


#pragma once


#include <string>
#include <valarray>
#include <vector>
#include "../generic/primitive_task.h"
#include "maxq_task.h"


namespace maxq_primitive_task {
const double TABULAR_ALPHA = 0.1;   // Learning rate for tables
const double LINEAR_ALPHA = 0.001;   // Learning rate for linear function approximators
}


class MaxQ_PrimTask : public PrimTask, public MaxQ_Task
{	public:
		enum Value_function_type {Tabular, Linear};

	protected:
		Value_function_type _v_type;
		valarray<double> _V;   // V(s) table
		valarray<double> _weight;   // Weights

		valarray<double> feature_vector(const unsigned& agent, const State& state) const;

	public:
		MaxQ_PrimTask(const string& task_name, const string& state_variables, const int& action, const MDP& mdp, const Value_function_type v_type = Tabular);
		void initialize();
		double V(const unsigned& agent, const vector<int>& parameters, const State& state) const;
		void update(const unsigned& agent, const State& state, const double& reward);
		string print(const State& state) const;
		~MaxQ_PrimTask() {}
};
