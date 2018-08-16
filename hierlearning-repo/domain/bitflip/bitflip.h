
/*************************************************

	BITFLIP DOMAIN
		Neville Mehta

**************************************************/


/*------------------------------------------------------------------------

State variables: bit_0, bit_1, ..., bit_n
Actions: Flip_0, Flip_1, ..., Flip_n
	If i < n then Flip_i toggles bit_i iff all the bits below it are set; otherwise, it resets bits 0--i
	Flip_n toggles bit_n iff the parity across bits 0--(n-1) equals the evenness of n and bit_(n-1) is set; otherwise, it resets all bits
Reward: -1

-------------------------------------------------------------------------*/


#pragma once


#include <vector>
#include "../mdp.h"


struct Bitflip_State : public State
{	valarray<int> bits;

	Bitflip_State (const int& num_bits = 0) { bits.resize(num_bits, -1); }
	unique_ptr<State> clone () const { return unique_ptr<State>(new Bitflip_State(*this)); }
	unique_ptr<State> copy () const { return unique_ptr<State>(new Bitflip_State(bits.size())); }
	vector<int> variables() const;
	int variable_index(const string& variable_name) const;
	string variable_name(const int& variable_index) const;
	int variable_size(const int& variable_index) const;
	int variable(const int& variable_index) const;
	int& variable(const int& variable_index);
	map<int,int> variables_mapper() const;
	unsigned num_agent_variables () const { return bits.size(); }
	pair<bool,int> parse(string expression) const;
	void read(istream& in);
	string print() const;
	bool left_bits_set(const int& index) const;
	bool parity(const int& index) const;
};


class Bitflip : public MDP
{	public:
		Bitflip(const string& name, const unsigned& num_bits) : MDP(name, new Bitflip_State(num_bits)) { initialize(); }
		const Bitflip_State& state () const { return *static_cast<Bitflip_State*>(_state); }   // Covariant return type for easy access
		Bitflip_State& state () { return *static_cast<Bitflip_State*>(_state); }   // Covariant return type for easy access
		vector<int> actions() const;
		void initialize(const bool& target = false);
		void process(const vector<int>& action);
		bool terminated () const { return state().left_bits_set((int)state().bits.size()); }
		int action_index(const string& action_name) const;
		string print_action (const int& action) const { return "Flip_" + to_string(action); }
		~Bitflip () {}
};
