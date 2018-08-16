
/***************************************

	COMMON FUNCTIONS
		Neville Mehta

****************************************/


#include <fstream>
#include <random>
#include <sys/stat.h>
#include "common.h"


namespace
{	random_device rand_dev;
	mt19937 rand_engine(rand_dev());
	uniform_real_distribution<double> ur_dist;
	normal_distribution<double> n_dist;
}


void rand_seed (const unsigned& index)
{	rand_engine.seed(index + 1);
}


unsigned rand_int (const unsigned& range)   // Integer random value {0, ..., range - 1}
{	uniform_int_distribution<unsigned> ui_dist(0, range - 1);
	return ui_dist(rand_engine);
}


double rand_real ()   // Real random value [0, 1)
{	return ur_dist(rand_engine);
}


double rand_norm ()   // Standard Normal random value
{	return n_dist(rand_engine);
}


#if !defined(__unix__)
char* optarg;
int optind = 0;
static char* scan = 0;

static char* index (const char* str, int chr)
{	while (*str)
	{	if (*str == chr)
			return (char*)str;
		++str;
	}

	return 0;
}

int getopt (int argc, char* argv[], char* optstring)
{	register char c;
	register char* place;
	optarg = 0;

	if (scan == 0 || *scan == '\0')
	{	if (optind == 0)
			++optind;

		if (optind >= argc || argv[optind][0] != '-' || argv[optind][1] == '\0')
			return -1;
		if (strcmp(argv[optind], "--") == 0)
		{	++optind;
			return -1;
		}

		scan = argv[optind] + 1;
		++optind;
	}

	c = *scan++;
	place = index(optstring, c);

	if (place == 0 || c == ':')
		return '?';

	++place;
	if (*place == ':')
	{	if (*scan != '\0')
		{	optarg = scan;
			scan = 0;
		}
		else if (optind < argc)
		{	optarg = argv[optind];
			++optind;
		}
		else
			return '?';
	}

	return c;
}
#endif


istream& operator >> (istream& in, Coordinate& coord)
{	string reader;
	if (in >> reader)
	{	string::size_type start = reader.find_first_not_of("(");
		string::size_type end = reader.find_first_of(",", start);
		coord.x = (reader.substr(start, end - start) == "*") ? -1 : from_string<int>(reader.substr(start, end - start));

		start = end + 1;
		end = reader.find_first_of(")", start);
		coord.y = (reader.substr(start, end - start) == "*") ? -1 : from_string<int>(reader.substr(start, end - start));
	}

	return in;
}


ostream& operator << (ostream& out, const Coordinate& coord)
{	out << "(" << coord.x << "," << coord.y << ")";
	return out;
}


void getline_os (istream& in, string& str)
{	if (in.bad())
		throw HierException(__FILE__, __LINE__, "Bad istream.");
	getline(in, str);
	if (!str.empty() && str[str.size() - 1] == '\r')
		str.resize(str.size() - 1);
}


string replace (const string& str, const string& from, const string& to)
{	string new_str(str);
	string::size_type pos = 0;
	while ((pos = new_str.find(from, pos)) != string::npos)
	{	new_str.replace(pos, from.length(), to);
		pos += from.length();
	}
	return new_str;
}


vector<string> tokenize (const string& str, const string& token_separators)
{	vector<string> tokens;
	string::size_type start = str.find_first_not_of(token_separators, 0);
	string::size_type end = str.find_first_of(token_separators, start);
	while (start != string::npos)
	{	tokens.push_back(str.substr(start, end - start));
		start = str.find_first_not_of(token_separators, end);
		end = str.find_first_of(token_separators, start);
	}
	return tokens;
}


bool entity_exists (const string& name)
{	struct stat entity_info; 
	if (stat(name.c_str(), &entity_info))
		return false;
	return true;
}


void create_directory (const string& directory)
{	if (!directory.empty() && !entity_exists(directory))
	{
		#if defined __unix__
		if (system(("mkdir -p " + directory).c_str()))
		#else
		string directory_windows = replace(directory, "/", "\\");
		if (system(("mkdir " + directory_windows).c_str()))
		#endif
			throw HierException(__FILE__, __LINE__, "Unable to create the " + directory + " directory");
	}
}


void remove_directory (const string& directory)
{	if (entity_exists(directory))
	{
		#if defined __unix__
		if (system(("rm -r " + directory).c_str()))
		#else
		string directory_windows = replace(directory, "/", "\\");
		if (system(("rmdir /s /q " + directory_windows).c_str()))
		#endif
			cerr << "(" + to_string(__FILE__) + ":" + to_string(__LINE__) + ")\n";
	}
}


void file_table (const valarray<double>& table, const string& filename)
{	ofstream output(filename.c_str());
	if (!output.is_open())
		throw HierException(__FILE__, __LINE__, "Unable to write to " + filename);
	for (unsigned i = 0; i < table.size(); ++i)
		output << i << "\t" << table[i] << endl;
	output.close();
}


bool is_equal_double (const double& a, const double& b)
{	// Best way to compare floating point numbers
	if (fabs(a - b) < 1e-8   // absolute error
		&& ((b > 0 && a > b * (1 - 1e10) && a < b * (1 + 1e10)) || (b < 0 && a > b * (1 + 1e10) && a < b * (1 - 1e10))))   // relative error
		return true;
	return false;
}
