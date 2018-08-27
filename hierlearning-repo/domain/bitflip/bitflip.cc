
/*************************************************

	BITFLIP DOMAIN
		Neville Mehta

**************************************************/


#include <sstream>
#include "../../lib/common.h"
#include "bitflip.h"



vector<int> Bitflip_State::variables () const
{	vector<int> state_variables;
	for (int b = 0; b < (int)bits.size(); ++b)
		state_variables.push_back(b);
	return state_variables;
}


// Variable name to integer index
int Bitflip_State::variable_index (const string& variable_name) const
{	if (variable_name.substr(0,3) == "bit")
	{	int index = from_string<int>(variable_name.substr(variable_name.find_first_of("_") + 1));
		if (index >= 0 && index < (int)bits.size())
			return index;
	}

	throw HierException(__FILE__, __LINE__, "Unknown variable: " + variable_name);
}


string Bitflip_State::variable_name (const int& variable_index) const
{	if (variable_index >= 0 && variable_index < (int)bits.size())
		return "bit_" + to_string(variable_index);
	throw HierException(__FILE__, __LINE__, "Unknown variable: " + to_string(variable_index));
}


// Arity of a variable
int Bitflip_State::variable_size (const int& variable_index) const
{	if (variable_index >= 0 && variable_index < (int)bits.size())
		return 2;
	throw HierException(__FILE__, __LINE__, "Unknown variable: " + to_string(variable_index));
}

int Bitflip_State::variable_size_for_state (const int& variable_index) const
{
	return variable_size(variable_index);
}


int Bitflip_State::variable (const int& variable_index) const
{	if (variable_index >= 0 && variable_index < (int)bits.size())
		return bits[variable_index];
	throw HierException(__FILE__, __LINE__, "Unknown variable: " + to_string(variable_index));
}


int& Bitflip_State::variable (const int& variable_index)
{	if (variable_index >= 0 && variable_index < (int)bits.size())
		return bits[variable_index];
	throw HierException(__FILE__, __LINE__, "Unknown variable: " + to_string(variable_index));
}


map<int,int> Bitflip_State::variables_mapper () const
{	map<int,int> var_map;
	for (int b = 0; b < (int)bits.size(); ++b)
		var_map[b] = variable(b);
	return var_map;
}


pair<bool,int> Bitflip_State::parse (string expression) const
{	int total = 0;

	// Delete spaces
	while (expression.find(" ") != string::npos)
		expression.replace(expression.find(" "), 1, "");

	expression.insert(0, "+");   // Explicit sign for positive numbers at the start

	string::size_type start = expression.find_first_not_of("+-", 0);
	string::size_type end = expression.find_first_of("+-", start);
	while (start != string::npos || end != string::npos)
	{	istringstream token(expression.substr(start, end - start));
		int value;
		if (!(token >> value))   // 'token' is not an integer
		{	if (token.str() == "num_bits")
				value = (int)bits.size();
			else
				return make_pair(false, 0);
				//throw HierException(__FILE__, __LINE__, "Cannot parse " + expression);
		}
		total += (expression[start-1] == '+') ? value : -value;

		start = expression.find_first_not_of("+-", end);
		end = expression.find_first_of("+-", start);
	}

	return make_pair(true, total);
}


void Bitflip_State::read (istream& in)
{	for (unsigned b = 0; b < bits.size() && in >> bits[b]; ++b);
}


string Bitflip_State::print () const
{	ostringstream out;

	for (unsigned b = 0; b < bits.size(); ++b)
	{	if (b > 0)
			out << " ";

		if (bits[b] != -1)
			out << bits[b];
		else
			out << " *";
	}

	return out.str();
}


bool Bitflip_State::left_bits_set (const int& index) const
{	for (int b = 0; b < index; ++b)
		if (!bits[b])
			return false;
	return true;
}


// Odd parity when variable index is odd, and even when even
bool Bitflip_State::parity (const int& index) const
{	bool odd = (index % 2 == 0);
	for (int b = 0; b < index; ++b)
		odd ^= (bits[b] == 1);
	return odd;
}

//*****************************************************************************************

void Bitflip::initialize (const bool& target)
{	for (auto& bit : state().bits)
		bit = 0;
}


vector<int> Bitflip::actions () const
{	vector<int> mdp_actions;
	for (unsigned a = 0; a < state().bits.size(); ++a)
		mdp_actions.push_back(a);
	return mdp_actions;
}


void Bitflip::process (const vector<int>& action)
{	_reward = -1.0;
	//_reward = -pow(2.0, (int)state().bits.size() - 1 - action);
	//_reward = -pow(2.0, action);
	_duration = 1.0;

	if (action[0] < 0 || action[0] >= (int)state().bits.size())
		throw HierException(__FILE__, __LINE__, "Unknown action.");

	if ((action[0] < (int)state().bits.size() - 1 && state().left_bits_set(action[0]))
					|| (action[0] == (int)state().bits.size() - 1 && state().bits[action[0] - 1] && state().parity(action[0])))
		state().bits[action[0]] = 1 - state().bits[action[0]];   // Flip the bit
	else   // Reset all bits with indices 0--action
		for (int b = 0; b <= action[0]; ++b)
			state().bits[b] = 0;
}

int Bitflip_State::debug_num_states() const 
{
	throw HierException(__FILE__, __LINE__, "ERROR: debug num states is being used (needed for Cleanup)");
}


int Bitflip::action_index (const string& action_name) const
{	if (action_name.substr(0,5) == "Flip_")
		return from_string<int>(action_name.substr(5));
	throw HierException(__FILE__, __LINE__, "Unknown action name.");
}
