
/*************************************************

	TAXI (DROP-OFF ANYWHERE)
		Neville Mehta

	Modification:
		Passenger can be dropped off anywhere

**************************************************/


#pragma once

#include "taxi.h"


class Taxi_DA : public Taxi
{	public:
		Taxi_DA (const string& name, const double& success_probability) : Taxi(name, success_probability) {}
		void process(const vector<int>& action);
		~Taxi_DA () {}
};
