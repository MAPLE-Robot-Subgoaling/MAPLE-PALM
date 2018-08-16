
/*************************************************

	WARGUS DOMAIN WITH GOTO NAVIGATION
		Neville Mehta

**************************************************/


#include <sstream>
#include "wargus_goto.h"



vector<int> Wargus_Goto::actions () const
{	vector<int> mdp_actions;
	mdp_actions.push_back(MineGold);
	mdp_actions.push_back(ChopWood);
	mdp_actions.push_back(Deposit);

	for (const auto& site : state().regions.data())
		mdp_actions.push_back(Move + site.coord.x + site.coord.y * state().map_size().x);

	return mdp_actions;
}


void Wargus_Goto::initialize (const bool& target)
{	Wargus::initialize(target);
	
	// Primitive actions are successful within the effective_range.
	// We divide the map into as many regions of (2 * Wargus_State::effective_range + 1) x (2 * Wargus_State::effective_range + 1) cells and store their centroids.
	Coordinate num_regions = state().map_size() / (2 * state().effective_range + 1);
	Coordinate remainder = state().map_size() % (2 * state().effective_range + 1);
	num_regions += Coordinate(remainder.x ? 1 : 0, remainder.y ? 1 : 0);

	state().regions.resize(num_regions.x, num_regions.y);
	for (int y = 0; y < num_regions.y; ++y)
		for (int x = 0; x < num_regions.x; ++x)
		{	state().regions(x, y).left_up_offset = state().regions(x, y).right_down_offset = Coordinate(state().effective_range, state().effective_range);
			if (x == num_regions.x - 1 && remainder.x)
			{	state().regions(x, y).left_up_offset.x = remainder.x / 2;
				state().regions(x, y).right_down_offset.x = remainder.x - 1 - state().regions(x, y).left_up_offset.x;
			}
			if (y == num_regions.y - 1 && remainder.y)
			{	state().regions(x, y).left_up_offset.y = remainder.y / 2;
				state().regions(x, y).right_down_offset.y = remainder.y - 1 - state().regions(x, y).left_up_offset.y;
			}
			state().regions(x, y).coord = Coordinate(x * (2 * state().effective_range + 1) + state().regions(x, y).left_up_offset.x, y * (2 * state().effective_range + 1) + state().regions(x, y).left_up_offset.y);
		}
}


void Wargus_Goto::process (const vector<int>& actions)
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
			{	case MineGold:
					if (state().num_agents() > 1 || (state().resources_gathered.gold < state().resource_quotas.gold && state().peasants[agent].resource == Wargus_State::NL))
						harvest(state().peasants[agent].id, Wargus_State::Gold);
					break;

				case ChopWood:
					if (state().num_agents() > 1 || (state().resources_gathered.wood < state().resource_quotas.wood && state().peasants[agent].resource == Wargus_State::NL))
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

				default:
					move(state().peasants[agent].id, Coordinate((actions[agent] - Move) % state().map_size().x, (actions[agent] - Move) / state().map_size().x));
			}
		}
	}

	Wargus::process(actions);
}


int Wargus_Goto::action_index (const string& action_name) const
{	if (action_name == "MineGold")
		return MineGold;
	if (action_name == "ChopWood")
		return ChopWood;
	if (action_name == "Deposit")
		return Deposit;
	if (action_name.substr(0,5) == "Goto_")
	{	vector<string> coords = tokenize(action_name.substr(5), "_");
		return Move + from_string<int>(coords[0]) + from_string<int>(coords[1]) * state().map_size().x;
	}
	throw HierException(__FILE__, __LINE__, "Unknown action name.");
}


string Wargus_Goto::print_action (const int& action) const
{	switch (action)
	{	case MineGold:
			return "MineGold";
		case ChopWood:
			return "ChopWood";
		case Deposit:
			return "Deposit";
		default:
			return "Goto_" + to_string((action - Move) % state().map_size().x) + "_" + to_string((action - Move) / state().map_size().x);
	}
}
