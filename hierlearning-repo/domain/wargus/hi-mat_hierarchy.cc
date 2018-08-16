//***** Termination functions *****//

bool GotoGold_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_region_gold")) == 1;
}

bool GotoWood_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_region_wood")) == 1;
}

bool GotoTown_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_region_townhall")) == 1;
}

bool GetGold_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_resource")) == 1;
}

bool GetWood_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_resource")) == 2;
}

bool Put_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(agent * state.num_agent_variables() + state.variable_index("agent_resource")) == 0;
}

bool HarvestGold_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(state.variable_index("requisite_gold")) == 1;
}

bool HarvestWood_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(state.variable_index("requisite_wood")) == 1;
}

bool WargusRoot_termination (const unsigned& agent, const vector<int>& parameters, const State& state)
{	return state.variable(state.variable_index("requisite_gold")) == 1 && state.variable(state.variable_index("requisite_wood")) == 1;
}

//***** Constructor *****//
{
	vector<CompositeTask::Subtask> move;
	Task* mine_gold = 0, * chop_wood = 0, * deposit = 0;
	const Wargus_State& state = static_cast<const Wargus_State&>(mdp.state());
	move.resize(state.regions.size());
	for (unsigned site = 0; site < move.size(); ++site)
	{	move[site].link = new PrimTask(string("Goto") + "_" + to_string(state.regions.data()[site].coord.x) + "_" + to_string(state.regions.data()[site].coord.y),
				"agent_x, agent_y", Wargus_Goto_MDP::Move + state.regions.data()[site].coord.x + state.regions.data()[site].coord.y * state.map_size().x, PrimTask::Tabular, mdp);
	}
	for (unsigned m = 0; m < move.size(); ++m)
		Task_list.push_back(move[m].link);

	mine_gold = new PrimTask("MineGold", "agent_resource, agent_region_gold, requisite_gold", Wargus_Goto_MDP::MineGold, PrimTask::Tabular, mdp);
	Task_list.push_back(mine_gold);
	chop_wood = new PrimTask("ChopWood", "agent_resource, agent_region_wood, requisite_wood", Wargus_Goto_MDP::ChopWood, PrimTask::Tabular, mdp);
	Task_list.push_back(chop_wood);
	deposit = new PrimTask("Deposit", "agent_resource, agent_region_townhall", Wargus_Goto_MDP::Deposit, PrimTask::Tabular, mdp);
	Task_list.push_back(deposit);

	vector<CompositeTask::Subtask> subtasks(move);
	Task* GotoGold = new CompositeTask("GotoGold", "", "agent_x, agent_y", subtasks, &GotoGold_termination, Expression(), mdp);
	Task_list.push_back(GotoGold);
	Task* GotoWood = new CompositeTask("GotoWood", "", "agent_x, agent_y", subtasks, &GotoWood_termination, Expression(), mdp);
	Task_list.push_back(GotoWood);
	Task* GotoTown = new CompositeTask("GotoTown", "", "agent_x, agent_y", subtasks, &GotoTown_termination, Expression(), mdp);
	Task_list.push_back(GotoTown);
	
	subtasks.clear();
	subtasks.push_back(GotoGold);
	subtasks.push_back(mine_gold);
	Task* GetGold = new CompositeTask("GetGold", "", "agent_x, agent_y, agent_resource, agent_region_gold, requisite_gold", subtasks, &GetGold_termination, Expression(), mdp);
	Task_list.push_back(GetGold);

	subtasks.clear();
	subtasks.push_back(GotoTown);
	subtasks.push_back(deposit);
	Task* PutGold = new CompositeTask("PutGold", "", "agent_x, agent_y, agent_resource, agent_region_townhall", subtasks, &Put_termination, Expression(), mdp);
	Task_list.push_back(PutGold);

	subtasks.clear();
	subtasks.push_back(GotoWood);
	subtasks.push_back(chop_wood);
	Task* GetWood = new CompositeTask("GetWood", "", "agent_x, agent_y, agent_resource, agent_region_wood, requisite_wood", subtasks, &GetWood_termination, Expression(), mdp);
	Task_list.push_back(GetWood);

	subtasks.clear();
	subtasks.push_back(GotoTown);
	subtasks.push_back(deposit);
	Task* PutWood = new CompositeTask("PutWood", "", "agent_x, agent_y, agent_resource, agent_region_townhall", subtasks, &Put_termination, Expression(), mdp);
	Task_list.push_back(PutWood);

	subtasks.clear();
	subtasks.push_back(GetGold);
	subtasks.push_back(PutGold);
	Task* HarvestGold = new CompositeTask("HarvestGold", "", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_townhall, requisite_gold", subtasks, &HarvestGold_termination, Expression(), mdp);
	Task_list.push_back(HarvestGold);

	subtasks.clear();
	subtasks.push_back(GetWood);
	subtasks.push_back(PutWood);
	Task* HarvestWood = new CompositeTask("HarvestWood", "", "agent_x, agent_y, agent_resource, agent_region_wood, agent_region_townhall, requisite_wood", subtasks, &HarvestWood_termination, Expression(), mdp);
	Task_list.push_back(HarvestWood);

	// Root task
	subtasks.clear();
	subtasks.push_back(HarvestGold);
	subtasks.push_back(HarvestWood);
	Task_list.push_front(new CompositeTask("Resource_Gathering_Task", "", "agent_x, agent_y, agent_resource, agent_region_gold, agent_region_wood, agent_region_townhall, requisite_gold, requisite_wood", subtasks, &WargusRoot_termination, Expression(), mdp));
}
