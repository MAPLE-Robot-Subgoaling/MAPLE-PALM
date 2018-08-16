
/*************************************************

	TAXI (MOVING PASSENGER/DROPPOFF ANYWHERE)
		Neville Mehta

	Modification:
		Passenger moves within the taxi
		and can be dropped off anywhere

**************************************************/


#pragma once

#include "taxi.h"


class Taxi_MP_DA : public Taxi
{	public:
		Taxi_MP_DA (const string& name, const double& success_probability) : Taxi(name, success_probability) {}
		void process(const vector<int>& action);
		~Taxi_MP_DA () {}
};
