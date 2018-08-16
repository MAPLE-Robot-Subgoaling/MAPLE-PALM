
/*************************************************

	WARGUS DOMAIN WITH NSEW NAVIGATION
		Neville Mehta

**************************************************/


#include <sstream>
#include "wargus_nsew.h"



vector<int> Wargus_NSEW::actions () const
{	vector<int> mdp_actions;
	mdp_actions.push_back(North);
	mdp_actions.push_back(South);
	mdp_actions.push_back(East);
	mdp_actions.push_back(West);
	mdp_actions.push_back(MineGold);
	mdp_actions.push_back(ChopWood);
	mdp_actions.push_back(Deposit);
	return mdp_actions;
}


void Wargus_NSEW::process (const vector<int>& actions)
{	for (unsigned agent = 0; agent < state().num_agents(); ++agent)
	{	if (actions[agent] == -1)
			continue;

		if (!state().action_complete(agent))   // Ongoing primitive action
		{	_duration[agent]++;
			_reward[agent] += reward_default;
		}
		else
		{	_duration[agent] = 1.0;
			_reward[agent] = reward_default;

			switch (actions[agent])
			{	case North:
					if (state().peasants[agent].coord.y < state().map_size().y - 1)
						move(state().peasants[agent].id, state().peasants[agent].coord + Coordinate(0, 1));
					break;

				case South:
					if (state().peasants[agent].coord.y > 0)
						move(state().peasants[agent].id, state().peasants[agent].coord + Coordinate(0, -1));
					break;

				case East:
					if (state().peasants[agent].coord.x < state().map_size().x - 1)
						move(state().peasants[agent].id, state().peasants[agent].coord + Coordinate(1, 0));
					break;

				case West:
					if (state().peasants[agent].coord.x > 0)
						move(state().peasants[agent].id, state().peasants[agent].coord + Coordinate(-1, 0));
					break;

				case MineGold:
					if (state().resources_gathered.gold < state().resource_quotas.gold && state().peasants[agent].resource == Wargus_State::NL)
						harvest(state().peasants[agent].id, Wargus_State::Gold);
					break;

				case ChopWood:
					if (state().resources_gathered.wood < state().resource_quotas.wood && state().peasants[agent].resource == Wargus_State::NL)
						harvest(state().peasants[agent].id, Wargus_State::Wood);
					break;

				case Deposit:
					if (deposit(state().peasants[agent].id))
						if ((state().peasants[agent].resource == Wargus_State::Gold && state().resources_gathered.gold < state().resource_quotas.gold)
										|| (state().peasants[agent].resource == Wargus_State::Wood && state().resources_gathered.wood < state().resource_quotas.wood))
						{	_reward[agent] += reward_deposit;
							depositing_agents.push_back(agent);   // Stratagus increments the gold/wood store immediately
						}
					break;
			}
		}
	}

	Wargus::process(actions);
}


int Wargus_NSEW::action_index (const string& action_name) const
{	if (action_name == "North")
		return North;
	if (action_name == "South")
		return South;
	if (action_name == "East")
		return East;
	if (action_name == "West")
		return West;
	if (action_name == "MineGold")
		return MineGold;
	if (action_name == "ChopWood")
		return ChopWood;
	if (action_name == "Deposit")
		return Deposit;
	throw HierException(__FILE__, __LINE__, "Unknown action name.");
}


string Wargus_NSEW::print_action (const int& action) const
{	switch (action)
	{	case North:
			return "North";
		case South:
			return "South";
		case East:
			return "East";
		case West:
			return "West";
		case MineGold:
			return "MineGold";
		case ChopWood:
			return "ChopWood";
		case Deposit:
			return "Deposit";
		default:
			throw HierException(__FILE__, __LINE__, "Unknown action.");
	}
}
