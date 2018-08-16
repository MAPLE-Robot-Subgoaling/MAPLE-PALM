
/*************************************************

	WARGUS DOMAIN WITH GOTO NAVIGATION
		Neville Mehta

**************************************************/


#pragma once

#include "wargus.h"


class Wargus_Goto : public Wargus
{	public:
		enum Action {MineGold, ChopWood, Deposit, Move};

		Wargus_Goto(const string& name, ClientSocket* const socket, const string& source_map_name, const unsigned& num_source_maps, const Wargus_State::Resources& source_resource_quotas, const string& target_map_name, const unsigned& num_target_maps, const Wargus_State::Resources& target_resource_quotas, const int& speed, const int& cycles)
			: Wargus(name, socket, source_map_name, num_source_maps, source_resource_quotas, target_map_name, num_target_maps, target_resource_quotas, speed, cycles) { initialize(); }
		vector<int> actions() const;
		void initialize(const bool& target = false);
		void process(const vector<int>& actions);
		int action_index(const string& action_name) const;
		string print_action(const int& action) const;
		~Wargus_Goto () {}
};
