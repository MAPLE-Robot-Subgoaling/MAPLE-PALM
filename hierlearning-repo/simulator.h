
/*************************************************

	SIMULATOR
		Neville Mehta

**************************************************/


#include <memory>
#include "domain/mdp.h"
#include "learner/learner.h"


class Simulator
{	public:
		void episode(MDP& mdp, Learner& learner, const unsigned& num_runs, const unsigned& run_index, const unsigned& num_episodes, const bool averaging) const;
		void step(MDP& mdp, Learner& learner, const unsigned& num_runs, const unsigned& run_index, const unsigned& num_steps, const bool averaging) const;
		vector<vector<unique_ptr<State_Action_Reward>>> trajectory_generator(const string& trajectory_filename, MDP& mdp, Learner* const learner, const unsigned& num_trajectories) const;
};
