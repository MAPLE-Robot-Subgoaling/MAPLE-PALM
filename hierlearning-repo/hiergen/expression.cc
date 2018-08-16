
/*************************************************

	EXPRESSION
		Neville Mehta

**************************************************/


#include <algorithm>
#include <iterator>
#include "../domain/mdp.h"
#include "expression.h"


Expression::Expression (const string& expression, const State& state, const set<int>& relevant_variables)
{	string filtered_expression = replace(expression, " ", "");
	if (filtered_expression.empty())
		throw HierException(__FILE__, __LINE__, "Expression string is empty.");
	expression_parse(filtered_expression, state, relevant_variables);
}


void Expression::expression_parse (const string& expression, const State& state, const set<int>& relevant_variables)
{	if (expression.find(",") != string::npos)
	{	exop = And;
		value = 0;
		vector<string> expression_tokens = tokenize(expression, ",");
		for (const auto& expression_token : expression_tokens)
		{	Expression temp_expression(expression_token, state, relevant_variables);
			if (relevant_variables.empty() || relevant_variables.find(*temp_expression.lhs_variables().begin()) != relevant_variables.end())
				children.push_back(temp_expression);
		}
		if (children.size() == 1)
		{	Expression temp_expression(children.front());
			*this = temp_expression;   // Cannot directly assign the children due to memory issues
		}
	}
	else if (expression.find_first_of("<>!=") != string::npos)
	{	string::size_type start = expression.find_first_not_of("<>!=");
		string::size_type end = expression.find_first_of("<>!=", start);
		if (expression.substr(end, 1) == "=")
			exop = Equal;
		else if (expression.substr(end, 2) == "<=")
			exop = Less_Equal;
		else if (expression.substr(end, 2) == ">=")
			exop = Greater_Equal;
		else if (expression.substr(end, 1) == "<")
			exop = Less;
		else if (expression.substr(end, 1) == ">")
			exop = Greater;
		else if (expression.substr(end, 2) == "!=")
			exop = Not_Equal;
		else
			throw HierException(__FILE__, __LINE__, "Invalid expression operator.");
		value = 0;

		children.emplace_back(expression.substr(start, end - start), state, relevant_variables);
		start = expression.find_first_not_of("<>=", end);
		end = expression.find_first_of("<>=", start);
		children.emplace_back(expression.substr(start, end - start), state, relevant_variables);

		if (end != string::npos)
			throw HierException(__FILE__, __LINE__, "Bad expression.");
	}
	else
	{	pair<bool,int> parse_value = state.parse(expression);
		if (parse_value.first)
		{	exop = Value;
			value = parse_value.second;
		}
		else
		{	exop = Variable;
			value = state.variable_index(expression);
		}
	}
}


void Expression::generalized_condition (const vector<map<int,int>>& states, const set<int>& variables)
{	// Find the conditions for variables that are true for the entire set of states
	// The family of conditions a variable is: (1) equal to constant, (2) equal to another variable

	set<int> all_variables = extract_map_keys(states.front());
	set<int> condition_vars = variables.empty() ? all_variables : variables;

	// Constant on r.h.s.
	for (auto var = condition_vars.cbegin(); var != condition_vars.cend(); )
	{	int value = states.front().find(*var)->second;
		bool equal_to_value = true;
		for (unsigned s = 1; s < states.size(); ++s)
			if (states[s].find(*var)->second != value)
			{	equal_to_value = false;
				break;
			}

		if (equal_to_value)   // var = value
		{	conjunct(Expression(*var, false, value));
			condition_vars.erase(var++);   // No need to find any more conditions for this variable
		}
		else
			++var;
	}

	// Variable on r.h.s.
	set<set<int>> equal_variables;   // Equivalence relation
	for (const auto& var1 : condition_vars)
		for (const auto& var2 : all_variables)
			if (var2 != var1)
			{	set<int> var_set = make_set<int>(2, var1, var2);
				bool equal_to_variable = false;
				for (const auto& ev : equal_variables)
					if (includes(ev.begin(), ev.end(), var_set.begin(), var_set.end()))
					{	equal_to_variable = true;
						break;
					}
				if (equal_to_variable)
					continue;

				equal_to_variable = true;
				for (const auto& state : states)
					if (state.find(var1)->second != state.find(var2)->second)
					{	equal_to_variable = false;
						break;
					}

				if (equal_to_variable)   // var1 = var2
				{	conjunct(Expression(var1, true, var2));

					bool matching_block_found = false;
					for (auto ev_itr = equal_variables.cbegin(); ev_itr != equal_variables.cend(); ++ev_itr)
						if (ev_itr->find(var1) != ev_itr->end() || ev_itr->find(var2) != ev_itr->end())
						{	set<int> block = *ev_itr;
							block.insert(var_set.begin(), var_set.end());
							equal_variables.erase(ev_itr);
							equal_variables.insert(block);
							matching_block_found = true;
							break;
						}
					if (!matching_block_found)
						equal_variables.insert(var_set);
				}
			}
}


Expression::Expression (const int& lhs_variable_index, const bool& rhs_variable, const int& index_or_value) : exop(Equal), value(0)
{	children.emplace_back(Variable, lhs_variable_index);
	if (rhs_variable)
		children.emplace_back(Variable, index_or_value);
	else
		children.emplace_back(Value, index_or_value);
}


Expression::Expression (const vector<Expression>& conditions) : exop(NoOp), value(0)
{	// Constructs a conjunction of factors that are present in all the conjunctive conditions

	if (conditions.empty())
		return;

	vector<Expression> factors = conditions.front().factorize();
	for (const auto& factor : factors)
	{	bool global_factor = true;
		for (const auto& condition : conditions)
		{	if (condition.exop == Or)
				throw HierException(__FILE__, __LINE__, "Expression is a disjunction.");

			if (!condition.subsumes(factor))
			{	global_factor = false;
				break;
			}
		}

		if (global_factor)
			conjunct(factor);
	}
}


int Expression::evaluate (const State& state, const vector<int>& parameters, const unsigned& agent) const
{	switch (exop)
	{	case NoOp:
			throw HierException(__FILE__, __LINE__, "Nothing to evaluate.");

		case Value:
			return value;

		case Variable:
			return state.variable(value);

		case Agent_Variable:
			return state.variable(agent * state.num_agent_variables() + value);

		case Parameter:
			if (value >= (int)parameters.size())
				throw HierException(__FILE__, __LINE__, "Expression parameter not found.");
			return parameters[value];

		case Equal:
			return children.front().evaluate(state, parameters, agent) == children.back().evaluate(state, parameters, agent);

		case Less_Equal:
			return children.front().evaluate(state, parameters, agent) <= children.back().evaluate(state, parameters, agent);

		case Greater_Equal:
			return children.front().evaluate(state, parameters, agent) >= children.back().evaluate(state, parameters, agent);

		case Less:
			return children.front().evaluate(state, parameters, agent) < children.back().evaluate(state, parameters, agent);

		case Greater:
			return children.front().evaluate(state, parameters, agent) > children.back().evaluate(state, parameters, agent);

		case Not_Equal:
			return children.front().evaluate(state, parameters, agent) != children.back().evaluate(state, parameters, agent);

		case And:
			for (const auto& child : children)
				if (!child.evaluate(state, parameters, agent))
					return 0;
			return 1;

		case Or:
			for (const auto& child : children)
				if (child.evaluate(state, parameters, agent))
					return 1;
			return 0;

		default:
			throw HierException(__FILE__, __LINE__, "Unknown expression operator");
	}
}


int Expression::evaluate (const map<int,int>& variables_map) const
{	switch (exop)
	{	case NoOp:
			throw HierException(__FILE__, __LINE__, "Nothing to evaluate.");

		case Value:
			return value;

		case Variable:
		{	auto v_itr = variables_map.find(value);
			if (v_itr == variables_map.end())
				throw HierException(__FILE__, __LINE__, "Expression variable not found.");
			return v_itr->second;
		}

		case Equal:
			return children.front().evaluate(variables_map) == children.back().evaluate(variables_map);

		case Less_Equal:
			return children.front().evaluate(variables_map) <= children.back().evaluate(variables_map);

		case Greater_Equal:
			return children.front().evaluate(variables_map) >= children.back().evaluate(variables_map);

		case Less:
			return children.front().evaluate(variables_map) < children.back().evaluate(variables_map);

		case Greater:
			return children.front().evaluate(variables_map) > children.back().evaluate(variables_map);

		case Not_Equal:
			return children.front().evaluate(variables_map) != children.back().evaluate(variables_map);

		case And:
			for (const auto& child : children)
				if (!child.evaluate(variables_map))
					return 0;
			return 1;

		case Or:
			for (const auto& child : children)
				if (child.evaluate(variables_map))
					return 1;
			return 0;

		default:
			throw HierException(__FILE__, __LINE__, "Unknown expression operator");
	}
}


bool Expression::operator == (const Expression& expression) const
{	if (children.size() != expression.children.size())
		return false;

	switch (exop)
	{	case NoOp:
			return exop == expression.exop;

		case Value:
		case Variable:
		case Agent_Variable:
		case Parameter:
			return exop == expression.exop && value == expression.value;

		case Equal:
		case Not_Equal:
			return exop == expression.exop && ((children.front() == expression.children.front() && children.back() == expression.children.back())
											|| (children.front() == expression.children.back() && children.back() == expression.children.front()));

		case Less_Equal:
			return (exop == expression.exop && children.front() == expression.children.front() && children.back() == expression.children.back())
				|| (expression.exop == Greater_Equal && children.front() == expression.children.back() && children.back() == expression.children.front());

		case Greater_Equal:
			return (exop == expression.exop && children.front() == expression.children.front() && children.back() == expression.children.back())
				|| (expression.exop == Less_Equal && children.front() == expression.children.back() && children.back() == expression.children.front());

		case Less:
			return (exop == expression.exop && children.front() == expression.children.front() && children.back() == expression.children.back())
				|| (expression.exop == Greater && children.front() == expression.children.back() && children.back() == expression.children.front());

		case Greater:
			return (exop == expression.exop && children.front() == expression.children.front() && children.back() == expression.children.back())
				|| (expression.exop == Less && children.front() == expression.children.back() && children.back() == expression.children.front());

		case And:
		case Or:
		{	if (exop != expression.exop)
				return false;

			// Accounting for the commutative property
			vector<int> subexp_index;   // Indexes the unmatched children of the rhs expression
			for (unsigned i = 0; i < expression.children.size(); ++i)
				subexp_index.push_back(i);

			bool matched = false;
			for (const auto& child : children)
			{	matched = false;
				for (unsigned i = 0; i < subexp_index.size(); ++i)
					if (child == expression.children[subexp_index[i]])
					{	subexp_index.erase(subexp_index.begin() + i);   // Removing matched child
						matched = true;
						break;
					}
				if (!matched)
					break;
			}
			if (matched || equivalence_equality(expression))
				return true;
			return false;
		}

		default:
			throw HierException(__FILE__, __LINE__, "Unknown expression operator.");
	}
}


bool Expression::parameter_binding_equality (const Expression& expression, map<int,int>& parameter_map) const
{	// Returns true if there exists an equality-making mapping from variables in this to the variables in expression

	if (children.size() != expression.children.size())
		return false;

	switch (exop)
	{	case NoOp:
			return exop == expression.exop;

		case Value:
		case Variable:
		case Agent_Variable:
			return exop == expression.exop && value == expression.value;

		case Parameter:
			if (expression.exop == Parameter)
			{	map<int,int>::const_iterator p_itr = parameter_map.find(value);
				if (p_itr != parameter_map.end())
					return p_itr->second == value;
				else
				{	parameter_map[value] = expression.value;
					return true;
				}
			}
			return false;

		case Equal:
		case Not_Equal:
			return exop == expression.exop && ((children.front().parameter_binding_equality(expression.children.front(), parameter_map) && children.back().parameter_binding_equality(expression.children.back(), parameter_map))
											|| (children.front().parameter_binding_equality(expression.children.back(), parameter_map) && children.back().parameter_binding_equality(expression.children.front(), parameter_map)));

		case Less_Equal:
			return (exop == expression.exop && children.front().parameter_binding_equality(expression.children.front(), parameter_map) && children.back().parameter_binding_equality(expression.children.back(), parameter_map))
				|| (expression.exop == Greater_Equal && children.front().parameter_binding_equality(expression.children.back(), parameter_map) && children.back().parameter_binding_equality(expression.children.front(), parameter_map));

		case Greater_Equal:
			return (exop == expression.exop && children.front().parameter_binding_equality(expression.children.front(), parameter_map) && children.back().parameter_binding_equality(expression.children.back(), parameter_map))
				|| (expression.exop == Less_Equal && children.front().parameter_binding_equality(expression.children.back(), parameter_map) && children.back().parameter_binding_equality(expression.children.front(), parameter_map));

		case Less:
			return (exop == expression.exop && children.front().parameter_binding_equality(expression.children.front(), parameter_map) && children.back().parameter_binding_equality(expression.children.back(), parameter_map))
				|| (expression.exop == Greater && children.front().parameter_binding_equality(expression.children.back(), parameter_map) && children.back().parameter_binding_equality(expression.children.front(), parameter_map));

		case Greater:
			return (exop == expression.exop && children.front().parameter_binding_equality(expression.children.front(), parameter_map) && children.back().parameter_binding_equality(expression.children.back(), parameter_map))
				|| (expression.exop == Less && children.front().parameter_binding_equality(expression.children.back(), parameter_map) && children.back().parameter_binding_equality(expression.children.front(), parameter_map));

		case And:
		case Or:
		{	if (exop != expression.exop)
				return false;

			// Accounting for the commutative property
			vector<int> subexp_index;   // Indexes the unmatched children of the rhs expression
			for (unsigned i = 0; i < expression.children.size(); ++i)
				subexp_index.push_back(i);

			for (const auto& child : children)
			{	bool matched = false;
				for (unsigned i = 0; i < subexp_index.size(); ++i)
					if (child.parameter_binding_equality(expression.children[subexp_index[i]], parameter_map))
					{	subexp_index.erase(subexp_index.begin() + i);   // Removing matched child
						matched = true;
						break;
					}
				if (!matched)
					return false;
			}
			return true;
		}

		default:
			throw HierException(__FILE__, __LINE__, "Unknown expression operator.");
	}
}


bool Expression::equivalence_equality (const Expression& expression) const
{	vector<unsigned> lhs_subexp_index;   // Indexes the unmatched children of the lhs expression
	for (unsigned i = 0; i < children.size(); ++i)
		lhs_subexp_index.push_back(i);
	vector<unsigned> rhs_subexp_index;   // Indexes the unmatched children of the rhs expression
	for (unsigned i = 0; i < expression.children.size(); ++i)
		rhs_subexp_index.push_back(i);

	for (unsigned l = 0; l < lhs_subexp_index.size(); )
	{	if (children[lhs_subexp_index[l]].exop == Equal && children[lhs_subexp_index[l]].children.front().exop == Variable && children[lhs_subexp_index[l]].children.back().exop == Variable)
		{	++l;
			continue;
		}

		bool matched = false;
		for (unsigned r = 0; r < rhs_subexp_index.size(); ++r)
			if (children[lhs_subexp_index[l]] == expression.children[rhs_subexp_index[r]])
			{	lhs_subexp_index.erase(lhs_subexp_index.begin() + l);   // Removing matched child in lhs
				rhs_subexp_index.erase(rhs_subexp_index.begin() + r);   // Removing matched child in rhs
				matched = true;
				break;
			}
		if (!matched)
			++l;
	}
	if (lhs_subexp_index.empty() && rhs_subexp_index.empty())
		return true;

	Expression lhs(exop, value);
	for (const auto& index : lhs_subexp_index)
		lhs.children.push_back(children[index]);
	set<set<int>> lhs_equivalence = lhs.equivalence();
	if (lhs_equivalence.empty())
		return false;

	Expression rhs(expression.exop, expression.value);
	for (const auto& index : rhs_subexp_index)
		rhs.children.push_back(expression.children[index]);
	set<set<int>> rhs_equivalence = rhs.equivalence();
	if (rhs_equivalence.empty())
		return false;

	if (lhs_equivalence == rhs_equivalence)
		return true;
	return false;
}


set<set<int>> Expression::equivalence () const
{	// Returns the equivalence relation

	set<set<int>> equal_variables;
	for (const auto& child : children)
		if (child.exop == Equal && child.children.front().exop == Variable && child.children.back().exop == Variable)
		{	bool matching_block_found = false;
			for (auto ev_itr = equal_variables.cbegin(); ev_itr != equal_variables.cend(); ++ev_itr)
				if (ev_itr->find(child.children.front().value) != ev_itr->end() || ev_itr->find(child.children.back().value) != ev_itr->end())
				{	set<int> block = *ev_itr;
					block.insert(child.children.front().value);
					block.insert(child.children.back().value);
					equal_variables.erase(ev_itr);
					equal_variables.insert(block);
					matching_block_found = true;
					break;
				}
			if (!matching_block_found)
				equal_variables.insert(make_set<int>(2, child.children.front().value, child.children.back().value));
		}

	return equal_variables;
}


bool Expression::subsumes (const Expression& expression) const
{	// Returns true if the argument is part of this expression

	if (exop == And || exop == Or)
	{	if (expression.exop == exop)
		{	vector<int> subexp_index;   // Indexes the unmatched children of the rhs expression
			for (unsigned i = 0; i < expression.children.size(); ++i)
				subexp_index.push_back(i);

			for (unsigned i = 0; i < subexp_index.size(); ++i)
			{	bool matched = false;
				for (const auto& child : children)
					if (child == expression.children[subexp_index[i]])
					{	subexp_index.erase(subexp_index.begin() + i);   // Removing matched child
						matched = true;
						break;
					}

				if (!matched)
					return false;
			}
			return true;
		}
		else
		{	for (const auto& child : children)
				if (child == expression)
					return true;
		}
	}
	else if (*this == expression)
		return true;

	return false;
}


set<int> Expression::lhs_variables () const
{	if (children.empty())
		return set<int>();

	set<int> variables;
	if (exop == And || exop == Or)
		for (const auto& child : children)
		{	set<int> child_abstraction = child.lhs_variables();
			variables.insert(child_abstraction.begin(), child_abstraction.end());
		}
	else if (children.front().exop == Variable)
		variables.insert(children.front().value);
	return variables;
}


Expression Expression::lhs_expression () const
{	if (exop != Equal && exop != Less_Equal && exop != Greater_Equal && exop != Less && exop != Greater && exop != Not_Equal)
		throw HierException(__FILE__, __LINE__, "No lhs expression.");
	return children.front();
}


set<int> Expression::rhs_variables () const
{	if (children.empty())
		return set<int>();

	set<int> variables;
	if (exop == And || exop == Or)
		for (const auto& child : children)
		{	set<int> child_abstraction = child.rhs_variables();
			variables.insert(child_abstraction.begin(), child_abstraction.end());
		}
	else if (children.back().exop == Variable)
		variables.insert(children.back().value);
	return variables;
}


Expression Expression::rhs_expression () const
{	if (exop != Equal && exop != Less_Equal && exop != Greater_Equal && exop != Less && exop != Greater && exop != Not_Equal)
		throw HierException(__FILE__, __LINE__, "No rhs expression.");
	return children.back();
}


set<int> Expression::variables () const
{	set<int> abstraction;

	if (children.empty())
	{	if (exop == Variable)
			abstraction.insert(value);
	}
	else
		for (const auto& child : children)
		{	set<int> child_abstraction = child.variables();
			abstraction.insert(child_abstraction.begin(), child_abstraction.end());
		}

	return abstraction;
}


int Expression::rhs_value () const
{	if (children.size() != 2 || children.back().exop != Value)
		throw HierException(__FILE__, __LINE__, "No rhs value.");

	return children.back().value;
}


bool Expression::variable_compared_to_constant (const int& variable) const
{	if (exop == Equal || exop == Less_Equal || exop == Greater_Equal || exop == Less || exop == Greater || exop == Not_Equal)
		if ((children.front().exop == Variable && children.front().value == variable && children.back().exop == Value)
			|| (children.back().exop == Variable && children.back().value == variable && children.front().exop == Value))
			return true;

	for (const auto& child : children)
		if (child.variable_compared_to_constant(variable))
			return true;
	return false;
}


void Expression::parameterize (const vector<int>& par_vars)
{	if (exop == Variable)
	{	// Search for variable
		auto v_itr = find(par_vars.begin(), par_vars.end(), value);
		if (v_itr != par_vars.end())
		{	exop = Parameter;
			value = v_itr - par_vars.begin();
			return;
		}
	}

	for (auto& child : children)
		child.parameterize(par_vars);
}


void Expression::set_agent_variables (const vector<int>& variables)
{	if (variables.empty())
		return;

	if (exop == Variable)
	{	// Search for variable
		for (const auto& var : variables)
			if (var == value)
			{	exop = Agent_Variable;
				return;
			}
	}

	for (auto& child : children)
		child.set_agent_variables(variables);
}


vector<Expression> Expression::factorize () const
{	if (exop == And)
	{	vector<Expression> expression_factors;
		for (const auto& child : children)
			expression_factors.push_back(child);
		return expression_factors;
	}
	else
		return vector<Expression>();
}


void Expression::conjunct (const Expression& expression)
{	if (expression.exop == NoOp || *this == expression)
		return;

	switch (exop)
	{	case NoOp:
			exop = expression.exop;
			value = expression.value;
			children = expression.children;
			return;

		case Value:
		case Variable:
		case Agent_Variable:
		case Parameter:
		case Equal:
		case Less_Equal:
		case Greater_Equal:
		case Less:
		case Greater:
		case Not_Equal:
			if (expression.exop == Or)   // X & (X | Y) = X
				for (const auto& child : expression.children)
					if (*this == child)
						return;

		case Or:
		{	// (X | Y) & X = X
			for (const auto& child : children)
				if (child == expression)
				{	exop = expression.exop;
					value = expression.value;
					children = expression.children;
					return;
				}

			Expression temp_expression = *this;
			children.clear();
			children.push_back(temp_expression);
			exop = And;
			value = 0;
			break;
		}

		case And:
			// (X & Y) & X = X & Y
			for (const auto& child : children)
				if (child == expression)
					return;
			break;
	}

	switch (expression.exop)
	{	case NoOp:   // Already checked, but here to placate gcc
		case Value:
		case Variable:
		case Agent_Variable:
		case Parameter:
		case Equal:
		case Less_Equal:
		case Greater_Equal:
		case Less:
		case Greater:
		case Not_Equal:
		case Or:
			children.push_back(expression);
			break;

		case And:
			for (const auto& child : expression.children)
				if (find(children.begin(), children.end(), child) == children.end())
					children.push_back(child);
			break;
	}

	if (children.size() == 1)
	{	Expression temp_expression = children.front();
		*this = temp_expression;
	}
}


void Expression::disjunct (const Expression& expression)
{	if (expression.exop == NoOp || *this == expression)
		return;

	switch (exop)
	{	case NoOp:
			exop = expression.exop;
			value = expression.value;
			children = expression.children;
			return;

		case Value:
		case Variable:
		case Agent_Variable:
		case Parameter:
		case Equal:
		case Less_Equal:
		case Greater_Equal:
		case Less:
		case Greater:
		case Not_Equal:
			if (expression.exop == And)   // X | (X & Y) = X
				for (const auto& child : expression.children)
					if (*this == child)
						return;

		case And:
		{	// (X & Y) | X = X
			for (const auto& child : children)
				if (child == expression)
				{	exop = expression.exop;
					value = expression.value;
					children = expression.children;
					return;
				}

			Expression temp_expression = *this;
			children.clear();
			children.push_back(temp_expression);
			exop = Or;
			value = 0;
			break;
		}

		case Or:
			// (X | Y) | Y = X | Y
			for (const auto& child : children)
				if (child == expression)
					return;
			break;
	}

	switch (expression.exop)
	{	case NoOp: // Already checked, but here to placate gcc
		case Value:
		case Variable:
		case Agent_Variable:
		case Parameter:
		case Equal:
		case Less_Equal:
		case Greater_Equal:
		case Less:
		case Greater:
		case Not_Equal:
		case And:
			children.push_back(expression);
			break;

		case Or:
			for (const auto& child : expression.children)
				if (find(children.begin(), children.end(), child) == children.end())
					children.push_back(child);
			break;
	}
}


void Expression::remove_factors (const Expression& expression)
{	if (exop != And || expression.empty())
		return;

	if (expression.exop == And)
	{	for (const auto& exp_child : expression.children)
			for (auto child_itr = children.begin(); child_itr != children.end(); ++child_itr)
				if (*child_itr == exp_child)
				{	children.erase(child_itr);
					break;
				}
	}
	else
	{	for (auto child_itr = children.begin(); child_itr != children.end(); ++child_itr)
			if (*child_itr == expression)
			{	children.erase(child_itr);
				break;
			}
	}

	if (children.empty())
	{	exop = NoOp;
		return;
	}

	if (children.size() == 1)
	{	Expression temp_expression(children.front());
		*this = temp_expression;
	}
}


void Expression::ground_rhs_variables (const map<int,int>& state)
{	switch (exop)
	{	case NoOp:
		case Value:
		case Parameter:
			return;

		case Variable:
			exop = Value;
			value = state.find(value)->second;
			break;

		case Equal:
		case Less_Equal:
		case Greater_Equal:
		case Less:
		case Greater:
		case Not_Equal:
			children.back().ground_rhs_variables(state);
			break;

		case And:
		case Or:
			for (auto& child : children)
				child.children.back().ground_rhs_variables(state);
			break;

		default:
			throw HierException(__FILE__, __LINE__, "Illegal grounding.");
	}
}


vector<Expression> Expression::exhaustive_grounding (const State& state) const
{	// Find all possible bindings of the variables on the rhs of the sub-expressions
	set<int> variables = rhs_variables();

	if (variables.empty())
		return vector<Expression>(1, *this);

	vector<pair<int, int>> variable_size;
	for (const auto& var : variables)
		variable_size.emplace_back(var, state.variable_size(var));

	vector<map<int,int>> possibilities;
	vector<int> var(variable_size.size(), 0);
	do
	{	possibilities.push_back(map<int,int>());
		for (unsigned i = 0 ; i < variable_size.size(); ++i)
			possibilities.back()[variable_size[i].first] = var[i];

		var[0]++;
		for (unsigned i = 0; i < var.size() - 1 && var[i] == variable_size[i].second; ++i)
		{	var[i] = 0;
			var[i+1]++;
		}
	} while (var.back() < (int)variable_size.back().second);
	if (possibilities.empty())
		throw HierException(__FILE__, __LINE__, "No ground expressions found.");

	// Create the grounded expressions
	vector<Expression> expression;
	for (const auto& possible : possibilities)
	{	expression.push_back(*this);
		expression.back().ground_rhs_variables(possible);
	}

	return expression;
}


void Expression::negate ()
{	switch (exop)
	{	case NoOp:
		case Value:
		case Variable:
		case Agent_Variable:
		case Parameter:
			return;

		case Equal:
			exop = Not_Equal;
			break;

		case Less_Equal:
			exop = Greater_Equal;
			break;

		case Greater_Equal:
			exop = Less_Equal;
			break;

		case Less:
			exop = Greater;
			break;

		case Greater:
			exop = Less;
			break;

		case Not_Equal:
			exop = Equal;
			break;

		case And:
			exop = Or;
			break;

		case Or:
			exop = And;
			break;
	}

	for (auto& child : children)
		child.negate();
}


void Expression::simplify ()
{	for (auto& child : children)
	{	child.simplify();

		// Checking single operands for And/Or
		if ((child.exop == And || child.exop == Or) && child.children.size() == 1)
			child = child.children.front();
	}

	switch (exop)
	{	case NoOp:
		case Value:
		case Variable:
		case Agent_Variable:
		case Parameter:
			break;

		case Equal:
		case Less_Equal:
		case Greater_Equal:
			if (children.front() == children.back())
			{	exop = Value;
				value = 1;
				children.clear();
			}
			break;

		case Less:
		case Greater:
		case Not_Equal:
			if (children.front() == children.back())
			{	exop = Value;
				value = 0;
				children.clear();
			}
			break;

		case And:
		case Or:
			// True/false operands (0 And X = 0, 1 And X = X; 0 Or X = X, 1 Or X = 1)
			for (auto child_itr = children.begin(); child_itr != children.end(); )
			{	if (child_itr->exop == Value)
				{	if (child_itr->value == (exop == And ? 0 : 1))
					{	exop = Value;
						value = exop == And ? 0 : 1;
						children.clear();
						break;
					}
					else if (child_itr->value == (exop == And ? 1 : 0))
						child_itr = children.erase(child_itr);
					else
						throw HierException(__FILE__, __LINE__, "Illegal value in expression.");
				}
				else
					++child_itr;
			}

			// Replicated operands
			for (auto child_itr1 = children.begin(); child_itr1 != children.end(); ++child_itr1)
			{	auto child_itr2 = child_itr1 + 1;
				while (child_itr2 != children.end())
				{	if (*child_itr1 == *child_itr2)
						child_itr2 = children.erase(child_itr2);
					else
						++child_itr2;
				}
			}
			break;
	}
}


string Expression::print () const
{	switch (exop)
	{	case NoOp:
			return "";

		case Value:
			return to_string(value);

		case Variable:
			return "v" + to_string(value);

		case Agent_Variable:
			return "av" + to_string(value);

		case Parameter:
			return "x" + to_string(value);

		case Equal:
			return children.front().print() + " = " + children.back().print();

		case Less_Equal:
			return children.front().print() + " <= " + children.back().print();

		case Greater_Equal:
			return children.front().print() + " >= " + children.back().print();

		case Less:
			return children.front().print() + " < " + children.back().print();

		case Greater:
			return children.front().print() + " > " + children.back().print();

		case Not_Equal:
			return children.front().print() + " != " + children.back().print();

		case And:
		case Or:
		{	auto child_itr = children.cbegin();
			string expression_str = "(" + child_itr->print();
			++child_itr;
			for ( ; child_itr != children.cend(); ++child_itr)
				expression_str += (exop == And ? " & " : " | ") + child_itr->print();
			expression_str += ")";
			return expression_str;
		}

		default:
			throw HierException(__FILE__, __LINE__, "Unknown expression operator.");
	}
}


string Expression::print_html () const
{	switch (exop)
	{	case NoOp:
			return "";

		case Value:
			return to_string(value);

		case Variable:
			return "<i>v</i><sub>" + to_string(value) + "</sub>";

		case Agent_Variable:
			return "<i>av</i><sub>" + to_string(value) + "</sub>";

		case Parameter:
			return "<i>x</i><sub>" + to_string(value) + "</sub>";

		case Equal:
			return children.front().print_html() + " = " + children.back().print_html();

		case Less_Equal:
			return children.front().print_html() + " <= " + children.back().print_html();

		case Greater_Equal:
			return children.front().print_html() + " >= " + children.back().print_html();

		case Less:
			return children.front().print_html() + " < " + children.back().print_html();

		case Greater:
			return children.front().print_html() + " > " + children.back().print_html();

		case Not_Equal:
			return children.front().print_html() + " != " + children.back().print_html();

		case And:
		case Or:
		{	auto child_itr = children.cbegin();
			string expression_str = "(" + child_itr->print_html();
			++child_itr;
			for ( ; child_itr != children.cend(); ++child_itr)
				expression_str += (exop == And ? " &amp; " : " | ") + child_itr->print_html();
			expression_str += ")";
			return expression_str;
		}

		default:
			throw HierException(__FILE__, __LINE__, "Unknown expression operator.");
	}
}
