
/*************************************************

	EXPRESSION
		Neville Mehta

**************************************************/


#pragma once


#include <list>
#include <map>
#include <set>
#include "../lib/common.h"


struct State;

class Expression
{	enum Exop {NoOp = -1, Value, Variable, Agent_Variable, Parameter, Equal, Less_Equal, Greater_Equal, Less, Greater, Not_Equal, And, Or};
	Exop exop;   // Expression operator
	int value;   // Leaf
	vector<Expression> children;

	void expression_parse(const string& expression, const State& state, const set<int>& relevant_variables);
	void generalized_condition(const vector<map<int,int>>& states, const set<int>& variables);
	void ground_rhs_variables(const map<int,int>& state);
	bool variable_compared_to_constant(const int& variable) const;

	public:
		Expression(const Exop& exop = NoOp, const int& value = 0, const vector<Expression>& children = vector<Expression>()) : exop(exop), value(value), children(children) {}
		Expression(const string& expression, const State& state, const set<int>& relevant_variables = set<int>());
		Expression(const vector<map<int,int>>& states, const int& variable) : exop(NoOp), value(0) { generalized_condition(states, make_set<int>(1, variable)); }
		Expression(const vector<map<int,int>>& states, const set<int>& variables = set<int>()) : exop(NoOp), value(0) { generalized_condition(states, variables); }
		Expression(const int& lhs_variable_index, const bool& rhs_variable, const int& index_or_value);   // Quick equation: "variable = variable" or "variable = value"
		Expression(const vector<Expression>& conditions);
		int evaluate(const State& state, const vector<int>& parameters = vector<int>(), const unsigned& agent = 0) const;
		int evaluate(const map<int,int>& variables_map) const;
		bool empty () const { return exop == NoOp; }
		void clear () { exop = NoOp; value = 0; children.clear(); }
		bool operator == (const Expression& expression) const;
		bool operator != (const Expression& expression) const { return !(*this == expression); }
		bool parameter_binding_equality(const Expression& expression, map<int,int>& parameter_map) const;
		bool equivalence_equality(const Expression& expression) const;
		set<set<int>> equivalence() const;
		bool subsumes(const Expression& expression) const;
		set<int> lhs_variables() const;
		Expression lhs_expression() const;
		set<int> rhs_variables() const;
		Expression rhs_expression() const;
		int rhs_value () const;
		bool equality () const { return exop == Equal; }
		set<int> variables() const;   // Get all variables
		void parameterize(const vector<int>& variables);
		void set_agent_variables(const vector<int>& variables);
		vector<Expression> factorize() const;
		void conjunct(const Expression& expression);   // Conjunction of two expressions
		void disjunct(const Expression& expression);   // Disjunction of two expressions
		void remove_factors(const Expression& expression);   // Remove the factors in expression from this conjunction
		vector<Expression> exhaustive_grounding(const State& state) const;   // Ground the values of the variables (all possibilities)
		void negate();
		void simplify();
		string print() const;
		string print_html() const;
		~Expression () {}
};
