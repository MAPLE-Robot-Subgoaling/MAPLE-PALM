
/*************************************************

	TAXI (MOVING PASSENGER/DROPPOFF ANYWHERE)
		Neville Mehta

**************************************************/


#include "taxi_mp_da.h"


void Taxi_MP_DA::process (const vector<int>& action)
{	if (action[0] != Dropoff)
		Taxi::process(action);
	else   // Modified dropoff
	{	_reward = reward_default;
		_duration = 1.0;

		if (rand_real() < Pr_successful_execution)
		{	for (auto& passenger : state().passengers)
				if (passenger.in_taxi)
				{	passenger.in_taxi = false;
					passenger.location = state().taxi.location;   // Drop off the passenger
					if (state().taxi.location == passenger.destination)   // Taxi's at its passenger's destination
						_reward += reward_dropoff;
					else
						_reward += reward_illegal;
				}
		}
	}

	// Passengers move within the taxi
	for (auto& passenger : state().passengers)
		if (passenger.in_taxi)
			passenger.location = state().taxi.location;
}
