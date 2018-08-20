
/*************************************************

	SIMULATOR
		Neville Mehta

**************************************************/


#include <fstream>
#include <iostream>
#include <string>
#include <valarray>
#include "lib/common.h"
#include "simulator.h"


#if defined(NDEBUG)
const bool display = false;
#else
const bool display = true;
#endif

const unsigned max_steps = 100000;


void Simulator::episode (MDP& mdp, Learner& learner, const unsigned& num_runs, const unsigned& run_index, const unsigned& num_episodes, const bool averaging) const
{	valarray<double> avg_reward(0.0, num_episodes + 1);   // Total reward through episode completion
	valarray<double> avg_duration(0.0, num_episodes + 1);   // Total duration through episode completion

	for (unsigned run = 0; run < num_runs; ++run)
	{	if (display) cout << "\nTrial " << run + 1 << endl << "Evaluating ...";
		mdp.initialize(true);
		learner.initialize(mdp);   // Initialize the learner's innards
		
		// Initial performance without learning
		double total_reward = 0.0;
		unsigned step = 0;
		while (!mdp.terminated())
		{	vector<int> action = learner.learned_policy(mdp.state());   // Exercise the learner's greedy policy
			mdp.process(action);
			total_reward += mdp.reward().sum();
			++step;

			if (step > max_steps)
				throw HierException(__FILE__, __LINE__, "Reached maximum number of steps.");
		}
		if (averaging)
		{	avg_reward[0] += (total_reward - avg_reward[0])/(run + 1);
			avg_duration[0] += (step - avg_duration[0])/(run + 1);
		}
		else
		{	avg_reward[0] = total_reward;
			avg_duration[0] = step;
		}

		for (unsigned episode = 1; episode <= num_episodes; ++episode)
		{	if (display) cout << " " << episode;
			mdp.initialize(true);
			learner.reset();
			total_reward = 0.0;
			step = 0;
			while (!mdp.terminated())
			{	vector<int> action = learner.exploratory_policy(mdp.state());   // Exercise the learner's exploratory policy
				mdp.process(action);   // Determine next state, reward, and time from the environment
				learner.update(mdp.state(), mdp.reward(), mdp.duration());
				total_reward += mdp.reward().sum();
				++step;

				if (step > max_steps)
					throw HierException(__FILE__, __LINE__, "Reached maximum number of steps.");
			}

			if (averaging)
			{	avg_reward[episode] += (total_reward - avg_reward[episode])/(run + 1);
				avg_duration[episode] += (step - avg_duration[episode])/(run + 1);
			}
			else
			{	avg_reward[episode] = total_reward;
				avg_duration[episode] = step;
			}
		}
		if (display) cout << ".\n";

		if (!averaging)
		{	file_table(avg_reward, "results/reward_" + mdp.name() + "_" + learner.name() + "_" + to_string(run_index + run) + ".out");
			file_table(avg_duration, "results/duration_" + mdp.name() + "_" + learner.name() + "_" + to_string(run_index + run) + ".out");
		}
	}

	// Write the average durations to a file
	if (averaging)
	{	file_table(avg_reward, "results/reward_" + mdp.name() + "_" + learner.name() + "_" + to_string(run_index) + ".out");
		file_table(avg_duration, "results/duration_" + mdp.name() + "_" + learner.name() + "_" + to_string(run_index) + ".out");
	}
}


void Simulator::step (MDP& mdp, Learner& learner, const unsigned& num_runs, const unsigned& run_index, const unsigned& num_steps, const bool averaging) const
{	const int averaging_interval = 100;
	valarray<double> avg_reward(0.0, num_steps/averaging_interval);   // Average reward through steps

	for (unsigned run = 0; run < num_runs; ++run)
	{	if (display) cout << "\nTrial " << run + 1 << endl << "Evaluating ...";
		mdp.initialize(true);
		learner.initialize(mdp);   // Initialize the learner's innards
		double average_reward = 0.0;		
		for (unsigned step = 0; step < num_steps; ++step)
		{	if (display && (step + 1) % averaging_interval == 0)  cout << " " << step/averaging_interval;
			vector<int> action = learner.exploratory_policy(mdp.state());   // Exercise the learner's exploratory policy
			mdp.process(action);   // Determine next state, reward, and time from the environment
			learner.update(mdp.state(), mdp.reward(), mdp.duration());
			average_reward += (mdp.reward().sum() - average_reward)/(step + 1);

			// Evaluation
			if ((step + 1) % averaging_interval == 0)
			{	int eval_index = step/averaging_interval;
				//for (unsigned eval_step = 0; eval_step < averaging_interval; ++eval_step)
				//{	if (mdp.terminated())
				//	{	mdp.initialize(true);
				//		learner.reset();
				//	}
				//	vector<int> action = learner.greedy_policy(mdp.state());
				//	mdp.process(action);
				//	average_reward += (mdp.reward().sum() - average_reward)/(averaging_interval * eval_index + eval_step + 1);
				//}
				if (averaging)
					avg_reward[eval_index] += (average_reward - avg_reward[eval_index])/(run + 1);
				else
					avg_reward[eval_index] = average_reward;
			}

			if (mdp.terminated())
			{	mdp.initialize(true);
				learner.reset();
			}
		}
		if (display) cout << ".\n";

		if (!averaging)
			file_table(avg_reward, "results/reward_" + mdp.name() + "_" + learner.name() + "_" + to_string(run_index + run) + ".out");
	}

	// Write the average durations to a file
	if (averaging)
		file_table(avg_reward, "results/reward_" + mdp.name() + "_" + learner.name() + "_" + to_string(run_index) + ".out");
}


vector<vector<unique_ptr<State_Action_Reward>>> Simulator::trajectory_generator (const string& trajectory_filename, MDP& mdp, Learner* const learner, const unsigned& num_trajectories) const
{	vector<vector<unique_ptr<State_Action_Reward>>> trajectories(num_trajectories);

	if (num_trajectories)
	{	vector<int> mdp_actions = mdp.actions();
		ofstream trajectory_file;
		if (!trajectory_filename.empty())
		{	size_t directory_index = trajectory_filename.find_last_of("/");
			if (directory_index != string::npos)
				create_directory(trajectory_filename.substr(0, directory_index));
			trajectory_file.open(trajectory_filename.c_str());
			if (!trajectory_file.is_open())
				throw HierException(__FILE__, __LINE__, "Cannot write to the trajectory file.");

			// Actions
			for (unsigned a = 0; a < mdp_actions.size(); ++a)
			{	if (a > 0) trajectory_file << " ";
				trajectory_file << mdp.print_action(mdp_actions[a]);
			}
			trajectory_file << endl;

			// Action indices
			for (unsigned a = 0; a < mdp_actions.size(); ++a)
			{	if (a > 0) trajectory_file << " ";
				trajectory_file << mdp_actions[a];
			}
			trajectory_file << endl;

			// Variables
			vector<int> state_variables = mdp.state().variables();
			for (auto v = state_variables.cbegin(); v != state_variables.cend(); ++v)
			{	if (v != state_variables.begin()) trajectory_file << " ";
				trajectory_file << mdp.state().variable_name(*v);
			}
			trajectory_file << endl;

			// Variable sizes
			for (auto v = state_variables.cbegin(); v != state_variables.cend(); ++v)
			{	if (v != state_variables.begin()) trajectory_file << " ";
				trajectory_file << mdp.state().variable_size(*v);
			}
			trajectory_file << "\n#\n";
		}

		if (display) cout << "Writing trajectories ...";
		for (unsigned t = 0; t < num_trajectories; ++t)
		{	if (display) cout << " " << t + 1;
			mdp.initialize(learner != nullptr);
			if (learner)
				learner->reset();

			while (!mdp.terminated())
			{	vector<int> action;
				if (learner)
					action = learner->learned_policy(mdp.state());
				else
				{	action = vector<int>(mdp.state().num_agents());
					for (unsigned a = 0; a < action.size(); ++a)
						if (mdp.state().action_complete(a))
							action[a] = mdp_actions[rand_int(mdp_actions.size())];
				}

				if (trajectory_file.is_open())
					trajectory_file << mdp.state().print() << endl << action[0] << endl;
				else
					trajectories[t].emplace_back(new State_Action_Reward(mdp.state(), action));

				if (trajectories[t].size() > 10) {
					cout << "";
				}
				//State_Action_Reward sar = (*trajectories[t].at(trajectories[t].size()-1));
				//cout << sar.reward;
				//cout << ",";
				//cout << sar.action;
				//cout << "\n";
				mdp.process(action);
				
				cout << "\n";
				cout << action;
				cout << " ";
				cout << mdp.reward()[0];
				cout << "\n";
				cout << mdp.state().print();
				cout << "\n";

				if (trajectory_file.is_open())
					trajectory_file << mdp.reward()[0] << endl;
				else
					trajectories[t].back()->reward = mdp.reward();
			}


			if (trajectory_file.is_open())
				trajectory_file << mdp.state().print() << "\n#\n";
			else
				trajectories[t].emplace_back(new State_Action_Reward(mdp.state(), vector<int>()));
		}
		if (trajectory_file.is_open())
			trajectory_file.close();
		if (display) cout << ".\n";
	}

	return trajectories;
}
