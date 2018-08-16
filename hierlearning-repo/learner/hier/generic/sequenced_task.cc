
/**********************************************************************************

	SEQUENCED TASK
		Neville Mehta

***********************************************************************************/


#include "sequenced_task.h"



unsigned SequencedTask::sequenced_subtask (const unsigned& agent, const vector<int>& parameters, const State& state) const
{	/* A subtask is admissible if:
		1. It is primitive.
		  OR
		1. The subtask's parameters can be grounded legally.
		2. The subtask is not terminated. */
	for (unsigned t = _subtask_index; t < _num_subtasks; ++t)
		if (_subtask[t].link->admissible(agent, subtask_bindings(agent, parameters, state, t), state))
			return t;

	throw HierException(__FILE__, __LINE__, "Task " + _name + " has no admissible child tasks.");
}
