
/*************************************************

	TAXI (MOVING PASSENGER)
		Neville Mehta

**************************************************/


#include "taxi_mp.h"


void Taxi_MP::process (const vector<int>& action)
{	Taxi::process(action);

	// Passengers move within the taxi
	for (auto& passenger : state().passengers)
		if (passenger.in_taxi)
			passenger.location = state().taxi.location;
}
