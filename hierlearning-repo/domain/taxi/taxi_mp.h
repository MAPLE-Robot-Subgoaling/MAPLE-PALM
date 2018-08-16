
/*************************************************

	TAXI (MOVING PASSENGER)
		Neville Mehta

	Modification:
		Passenger moves within the taxi

**************************************************/


#pragma once

#include "taxi.h"


class Taxi_MP : public Taxi
{	public:
		Taxi_MP (const string& name, const double& success_probability) : Taxi(name, success_probability) {}
		void process(const vector<int>& action);
		~Taxi_MP () {}
};
