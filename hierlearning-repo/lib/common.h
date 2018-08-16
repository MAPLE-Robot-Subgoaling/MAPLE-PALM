
/***************************************

	COMMON FUNCTIONS
		Neville Mehta

****************************************/


#pragma once

#include <cstdarg>
#include <exception>
#include <iostream>
#include <list>
#include <map>
#include <set>
#include <sstream>
#include <typeinfo>
#include <valarray>
#include <vector>
using namespace std;


void rand_seed(const unsigned& index);
unsigned rand_int(const unsigned& range);
double rand_real();
double rand_norm(const double& mean, const double& variance);
void getline_os(istream& in, string& str);
string replace(const string& str, const string& from, const string& to);
vector<string> tokenize(const string& str, const string& token_separators);
bool entity_exists(const string& name);   // File or directory
void create_directory(const string& directory);
void remove_directory(const string& directory);
void file_table(const valarray<double>& table, const string& fname);
bool is_equal_double(const double& a, const double& b);

#if defined(__unix__)
#include <unistd.h>
#else
extern int optind;
extern char* optarg;
int getopt(int argc, char* argv[], char* optstring);
#endif


class HierException : public exception
{	string filename;
	int line;
	string error;

	public:
		HierException (const string& filename, const int& line, const string& error) : filename(filename), line(line), error(error) {}
		const char* what () const throw()
		{	static string output;
			output = "\n" + error + " (" + filename + ":" + to_string(line) + ")";
			return output.c_str();
		}
		virtual ~HierException () throw() {}
};


struct Coordinate
{	int x;
	int y;

	Coordinate (const int& val = 0) : x(val), y(val) {}
	Coordinate (const int& x, const int& y) : x(x), y(y) {}
	bool operator == (const Coordinate& coord) const { return x == coord.x && y == coord.y; }
	bool operator != (const Coordinate& coord) const { return x != coord.x || y != coord.y; }   // Alternatively: return !(*this == coord);
	Coordinate operator + (const Coordinate& coord) const { return Coordinate(x + coord.x, y + coord.y); }
	Coordinate& operator += (const Coordinate& coord) { x += coord.x; y += coord.y; return *this; }
	template <typename T> Coordinate operator / (const T& val) const { return Coordinate(x / val, y / val); }
	template <typename T> Coordinate operator % (const T& val) const { return Coordinate(x % val, y % val); }
};

istream& operator >>(istream& in, Coordinate& coord);
ostream& operator <<(ostream& out, const Coordinate& coord);


template <typename T> string to_string (const T& thing)
{	ostringstream out_stream;
	out_stream << thing;
	return out_stream.str();
}


template <typename T> T from_string (const string& str)
{	istringstream in_stream(str);
	T value;
	if (in_stream >> value)
		return value;
	throw HierException(__FILE__, __LINE__, "Type " + to_string(typeid(T).name()) + " expected in the string \"" + str + "\".");
}


inline int combination (const int& n, const int& r)
{	int numerator = 1, denominator = 1;

	if (r < n / 2)
	{	for (int x = n - r + 1; x <= n; ++x)
			numerator *= x;
		for (int x = 2; x <= r; ++x)
			denominator *= x;
	}
	else
	{	for (int x = r + 1; x <= n; ++x)
			numerator *= x;
		for (int x = 2; x <= n - r; ++x)
			denominator *= x;
	}

	return numerator / denominator;
}


template <typename T> set<T> make_set (unsigned num_args, ...)
{	va_list vl;
	va_start(vl, num_args);
	set<T> s;
	while (num_args > 0)
	{	s.insert(va_arg(vl, T));
		--num_args;
	}
	va_end(vl);
	return s;
}


template <typename T> vector<T> make_vector (unsigned num_args, ...)
{	va_list vl;
	va_start(vl, num_args);
	vector<T> v;
	while (num_args > 0)
	{	v.push_back(va_arg(vl, T));
		--num_args;
	}
	va_end(vl);
	return v;
}


template <typename S, typename T> set<S> extract_map_keys (const map<S,T>& m)
{	set<S> keys;
	for (const auto& x : m)
		keys.insert(x.first);
	return keys;
}


template <typename S, typename T> set<S> extract_map_values (const map<S,T>& m)
{	set<T> values;
	for (const auto& x : m)
		values.insert(x.second);
	return values;
}


template <typename T> ostream& operator << (ostream& out, const valarray<T>& v)
{	out << "(";
	for (size_t x = 0; x < v.size(); ++x)
	{	if (x > 0)
			out << ", ";
		out << v[x];
	}
	out << ")";

	return out;
}


template <typename T> ostream& operator << (ostream& out, const vector<T>& v)
{	out << "(";
	for (auto x = v.cbegin(); x != v.cend(); ++x)
	{	if (x != v.begin())
			out << ",";
		out << *x;
	}
	out << ")";

	return out;
}


template <typename S, typename T> ostream& operator << (ostream& out, const map<S,T>& m)
{	out << "(";
	for (auto x = m.cbegin(); x != m.cend(); ++x)
	{	if (x != m.begin())
			out << ",";
		out << "(" << x->first << ":" << x->second << ")";
	}
	out << ")";

	return out;
}


template <typename T> ostream& operator << (ostream& out, const set<T>& s)
{	out << "{";
	for (auto x = s.cbegin(); x != s.cend(); ++x)
	{	if (x != s.begin())
			out << ",";
		out << *x;
	}
	out << "}";

	return out;
}
