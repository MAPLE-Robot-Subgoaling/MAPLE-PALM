
/*************************************************

	HIERARCHICAL LEARNER
		Neville Mehta

**************************************************/


#pragma once

#include "../../../domain/domains.h"


inline bool Root_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return false;
}


template <typename CTask, typename PTask, typename RTask>
void HierLearner::flat_hierarchy_designer (const MDP& mdp)
{	// Create the shallowest hierarchy (root task with primitive children) without smart abstraction
	if (mdp.name().substr(0,4) == "taxi")
	{	vector<CompositeTask::Subtask> move;
		move.push_back(new PTask("North", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", Taxi::North, mdp));
		move.push_back(new PTask("South", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", Taxi::South, mdp));
		move.push_back(new PTask("East", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", Taxi::East, mdp));
		move.push_back(new PTask("West", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", Taxi::West, mdp));
		Task* pickup = new PTask("Pickup", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", Taxi::Pickup, mdp);
		Task* dropoff = new PTask("Dropoff", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", Taxi::Dropoff, mdp);

		// Root task
		vector<CompositeTask::Subtask> subtasks(move);   // Attach primitive move tasks
		subtasks.push_back(pickup);
		subtasks.push_back(dropoff);
		Root_task = new RTask("Taxi_Task", "", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", subtasks, &Root_termination, Expression(), mdp);
	}
	else if (mdp.name().substr(0,7) == "bitflip")
	{	vector<int> mdp_actions = mdp.actions();
		string state_variables;
		for (unsigned a = 0; a < mdp_actions.size(); ++a)
		{	if (a > 0)
				state_variables += ", ";
			state_variables += "bit_" + to_string(mdp_actions[a]);
		}

		vector<CompositeTask::Subtask> subtasks;
		for (const auto& mdp_action : mdp_actions)
			subtasks.push_back(new PTask("Flip_" + to_string(mdp_action), state_variables, mdp_action, mdp));
		Root_task = new RTask("Bitflip_Task", "", state_variables, subtasks, &Root_termination, Expression(), mdp);
	}
	else if (mdp.name().substr(0,6) == "wargus")
	{	// Navigation			
		vector<CompositeTask::Subtask> move;
		Task* mine_gold = nullptr, * chop_wood = nullptr, * deposit = nullptr;
		if (mdp.name().substr(6) == "_goto")
		{	const Wargus_State& state = static_cast<const Wargus_State&>(mdp.state());
			move.resize(state.regions.size());
			for (unsigned site = 0; site < move.size(); ++site)
			{	move[site].link = new PTask(string("Goto") + "_" + to_string(state.regions.data()[site].coord.x) + "_" + to_string(state.regions.data()[site].coord.y),
						"agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood",
						Wargus_Goto::Move + state.regions.data()[site].coord.x + state.regions.data()[site].coord.y * state.map_size().x, mdp);
			}

			mine_gold = new PTask("MineGold", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_Goto::MineGold, mdp);
			chop_wood = new PTask("ChopWood", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_Goto::ChopWood, mdp);
			deposit = new PTask("Deposit", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_Goto::Deposit, mdp);
		}
		else if (mdp.name().substr(6) == "_nsew")
		{	move.push_back(new PTask("North", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_NSEW::North, mdp));
			move.push_back(new PTask("South", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_NSEW::South, mdp));
			move.push_back(new PTask("East", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_NSEW::East, mdp));
			move.push_back(new PTask("West", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_NSEW::West, mdp));

			mine_gold = new PTask("MineGold", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_NSEW::MineGold, mdp);
			chop_wood = new PTask("ChopWood", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_NSEW::ChopWood, mdp);
			deposit = new PTask("Deposit", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", Wargus_NSEW::Deposit, mdp);
		}

		// Root task
		vector<CompositeTask::Subtask> subtasks(move);
		subtasks.push_back(mine_gold);
		subtasks.push_back(chop_wood);
		subtasks.push_back(deposit);
		Root_task = new RTask("Resource_Gathering_Task", "", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", subtasks, &Root_termination, Expression(), mdp);
	}
	else
		throw HierException(__FILE__, __LINE__, "Unknown MDP.");
}


inline bool Goto_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return parameters[0] == state.variable(agent * state.num_agent_variables() + state.variable_index("agent_x"))
			&& parameters[1] == state.variable(agent * state.num_agent_variables() + state.variable_index("agent_y"));
}


inline bool Get_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(state.variable_index("passenger_in_taxi")) == 1;
}


inline bool Put_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(state.variable_index("passenger_in_taxi")) == 0;
}


inline bool Get_Goto_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_x")) == state.variable(state.variable_index("passenger_location_x"))
			&& state.variable(agent * state.num_agent_variables() + state.variable_index("agent_y")) == state.variable(state.variable_index("passenger_location_y"));
}


inline bool Put_Goto_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_x")) == state.variable(state.variable_index("passenger_destination_x"))
			&& state.variable(agent * state.num_agent_variables() + state.variable_index("agent_y")) == state.variable(state.variable_index("passenger_destination_y"));
}


template <typename CTask, typename PTask, typename RTask>
void HierLearner::task_hierarchy_designer (const string& type, const MDP& mdp)
{	if (type == "taxi:manual")
	{	vector<CompositeTask::Subtask> move;
		move.push_back(new PTask("North", "", Taxi::North, mdp));
		move.push_back(new PTask("South", "", Taxi::South, mdp));
		move.push_back(new PTask("East", "", Taxi::East, mdp));
		move.push_back(new PTask("West", "", Taxi::West, mdp));
		Task* pickup = new PTask("Pickup", "agent_x, agent_y, passenger_location_x, passenger_location_y", Taxi::Pickup, mdp);
		Task* dropoff = new PTask("Dropoff", "agent_x, agent_y, passenger_destination_x, passenger_destination_y", Taxi::Dropoff, mdp);

		Task* Goto = new CTask("Goto", "map_size.x, map_size.y", "agent_x, agent_y", move, Goto_termination, Expression(), mdp);

		vector<CompositeTask::Subtask> subtasks;
		subtasks.emplace_back(Goto, "passenger_location_x, passenger_location_y");
		subtasks.push_back(pickup);
		Task* Get = new CTask("Get", "", "agent_x, agent_y, passenger_location_x, passenger_location_y", subtasks, Get_termination, Expression(), mdp);

		subtasks.clear();
		subtasks.emplace_back(Goto, "passenger_destination_x, passenger_destination_y");
		subtasks.push_back(dropoff);
		Task* Put = new CTask("Put", "", "agent_x, agent_y, passenger_destination_x, passenger_destination_y", subtasks, Put_termination, Expression(), mdp);

		// Root task
		subtasks.clear();
		subtasks.push_back(Get);
		subtasks.push_back(Put);
		Root_task = new RTask("Taxi_Task", "", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", subtasks, &Root_termination, Expression(), mdp);
	}
	else if (type == "taxi:hi-mat")
	{	vector<CompositeTask::Subtask> move;
		move.push_back(new PTask("North", "", Taxi::North, mdp));
		move.push_back(new PTask("South", "", Taxi::South, mdp));
		move.push_back(new PTask("East", "", Taxi::East, mdp));
		move.push_back(new PTask("West", "", Taxi::West, mdp));
		Task* pickup = new PTask("Pickup", "agent_x, agent_y, passenger_location_x, passenger_location_y", Taxi::Pickup, mdp);
		Task* dropoff = new PTask("Dropoff", "agent_x, agent_y, passenger_destination_x, passenger_destination_y", Taxi::Dropoff, mdp);

		Task* Get_Goto = new CTask("Get_Goto", "", "agent_x, agent_y, passenger_location_x, passenger_location_y", move, Get_Goto_termination, Expression(), mdp);
		Task* Put_Goto = new CTask("Put_Goto", "", "agent_x, agent_y, passenger_destination_x, passenger_destination_y", move, Put_Goto_termination, Expression(), mdp);

		vector<CompositeTask::Subtask> subtasks;
		subtasks.push_back(Get_Goto);
		subtasks.push_back(pickup);
		Task* Get = new CTask("Get", "", "agent_x, agent_y, passenger_location_x, passenger_location_y", subtasks, Get_termination, Expression(), mdp);

		subtasks.clear();
		subtasks.push_back(Put_Goto);
		subtasks.push_back(dropoff);
		Task* Put = new CTask("Put", "", "agent_x, agent_y, passenger_destination_x, passenger_destination_y", subtasks, Put_termination, Expression(), mdp);

		// Root task
		subtasks.clear();
		subtasks.push_back(Get);
		subtasks.push_back(Put);
		Root_task = new RTask("Taxi_Task", "", "agent_x, agent_y, passenger_location_x, passenger_location_y, passenger_destination_x, passenger_destination_y, passenger_in_taxi", subtasks, &Root_termination, Expression(), mdp);
	}
	else
		throw HierException(__FILE__, __LINE__, "Hierarchy type \"" + type + "\" does not exist.");
}
