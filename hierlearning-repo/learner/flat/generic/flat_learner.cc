
/*****************************************************************************************

   	FLAT LEARNER
		Neville Mehta

******************************************************************************************/


#include <fstream>
#include "../../../lib/common.h"
#include "flat_learner.h"


string FlatLearner::mdp_state_variables (const MDP& mdp) const
{	vector<int> state_variables = mdp.state().variables();
	string state_variables_str;
	for (auto v = state_variables.cbegin(); v != state_variables.cend(); ++v)
	{	if (v != state_variables.begin())
			state_variables_str += ", ";
		state_variables_str += mdp.state().variable_name(*v);
	}
	return state_variables_str;
}


void FlatLearner::initialize (const MDP& mdp)
{	current_action.clear();
	current_action.resize(mdp.state().num_agents(), 0);
	function->initialize();
}


vector<int> FlatLearner::greedy_policy (const State& state)
{	current_state = state.clone();
	vector<int> agent_actions(state.num_agents());
	for (unsigned agent = 0; agent < state.num_agents(); ++agent)
		if (state.action_complete(agent))
		{	current_action[agent] = function->greedy_policy(agent, state);
			agent_actions[agent] = action_encoding[current_action[agent]];
		}
	return agent_actions;
}


vector<int> FlatLearner::exploratory_policy (const State& state)
{	current_state = state.clone();
	vector<int> agent_actions(state.num_agents());
	for (unsigned agent = 0; agent < state.num_agents(); ++agent)
		if (state.action_complete(agent))
		{	current_action[agent] = function->exploratory_policy(agent, state);
			agent_actions[agent] = action_encoding[current_action[agent]];
		}
	return agent_actions;
}


void FlatLearner::update (const State& next_state, const valarray<double>& reward, const valarray<double>& duration)
{	for (unsigned agent = 0; agent < next_state.num_agents(); ++agent)
		if (next_state.action_complete(agent))
			function->update(agent, *current_state, current_action[agent], reward[agent], duration[agent], next_state);
}


// Write the agent's task values to a file
void FlatLearner::write_text_file (const string& filename, const MDP& mdp) const
{	ofstream output(filename.c_str());
	if (!output.is_open())
		throw HierException(__FILE__, __LINE__, "Unable to write to " + filename);

	output << "Learner: " << _name << endl;
	output << "MDP: " << mdp.name() << endl;
	output << "Epsilon: " << function_parameters::EPSILON << endl;
	output << function->print(mdp.state());
	output.close();
}
