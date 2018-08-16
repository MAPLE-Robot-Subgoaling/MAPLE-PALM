
/*************************************************

	MAIN
		Neville Mehta

**************************************************/


#include <fstream>
#include <iostream>
#include <string>
#include "domain/domains.h"
#include "learner/learners.h"
#include "hiergen/hierarchy.h"
#include "simulator.h"


#if defined(NDEBUG)
const bool display = false;
#else
const bool display = true;
#endif


string usage (const char* prog_name)
{	string program_name = to_string(prog_name);
	return "USAGE:\n\t" + program_name + " [OPTION] ...\n"
	"\t" + program_name + " -d <domain> -l <learner> [-r <number of runs> -e <number of episodes>]\n"
	"\t" + program_name + " -d <domain> -l <learner> [-r <number of runs> -s <number of steps>]\n"
	"\t" + program_name + " -d <domain> -n <number of trajectories> -t <trajectory filename>\n"
	"\t" + program_name + " -d <domain> -l <learner> -n <number of trajectories> [-m <model directory>] [-r <number of runs> -e <number of episodes>]\n"
	"\t" + program_name + " -d <domain> -l <learner> -t <trajectory file> [-m <model directory>] [-r <number of runs> -e <number of episodes>]\n"
	"\n\n"
	"OPTIONS:\n"
	"\n"
	"-a\n"
	"\tNo averaging over the runs; every run will be output in a separate file.\n"
	"\n"
	"-d <domain>\n"
	"\tName of the sequential decision process.\n"
	"\t<domain> := taxi[_mp][_stoc] | wargus_goto | bitflip\n"
	"\n"
	"\ttaxi: regular\n"
	"\ttaxi_mp: passenger moves within taxi\n"
	"\t_stoc: stochastic variant of any domain (success probability < 1 is set in the code)\n"
	"\tbitflip: bitflip domain\n"
	"\twargus_goto: resource collection in Wargus where the primitive actions are temporally-extended navigation actions\n"
	"\n"
	"-e <number>\n"
	"\tNumber of simulation episodes; overrides the step switch.\n"
	"\n"
	"-h\n"
	"\tPrints usage information.\n"
	"\n"
	"-l <learner>\n"
	"\tName of the learning algorithm.\n"
	"\t<learner> := q | maxq | h | hh\n"
	"\n"
	"-o <number>\n"
	"\tRun index; useful for execution on a cluster.\n"
	"\n"
	"-r <number>\n"
	"\tNumber of simulation runs.\n"
	"\n"
	"-s <number>\n"
	"\tNumber of simulation timesteps; active only when the episode switch is absent or the number of episodes is zero.\n"
	"\n"
	"-z <number>\n"
	"\tRandom seed.\n"
	"\n\n"
	"HierGen:\n"
	"\n"
	"-m <model directory>\n"
	"\tName of the directory containing previously learned models.  When specified, the program avoids relearning the models from the input trajectories.\n"
	"\n"
	"-n <number>\n"
	"\tNumber of trajectories to be generated.  When specified (and nonzero), all switches but the trajectory filename are ignored.  If the trajectory filename is specified, then the generated trajectories are saved to the file and the program terminates.  If the trajectory filename is not specified, then the trajectories are generated in memory and the program continues.\n"
	"\n"
	"-t <trajectory filename>\n"
	"\tName of the trajectory file.  If the number of trajectories is specified (and nonzero), then it indicates the output file where the generated trajectories are saved.  Otherwise, it indicates the input file of trajectories.\n"
	"\n"
	"Note: If both the number of trajectories and the trajectory filename are not specified, then HierGen is not activated.  The hard-coded task structure is loaded for a hierarchical agent.\n"
	"\n";
}


int main (int argc, char* argv[])
{	try
	{	if (argc == 1)
		{	cout << usage(argv[0]);
			return 1;
		}
		
		// Unbuffered output
		cout.setf(ios::unitbuf);
		cerr.setf(ios::unitbuf);

		string domain_name, learner_name, trajectory_filename, model_directory;
		bool averaging = true;
		unsigned num_runs = 0, run_index = 1, num_episodes = 0, num_steps = 0, num_trajectories = 0;

		// Parse command line arguments
		char option_char;
		while ((option_char = getopt(argc, argv, "ad:e:hl:m:n:o:r:s:t:z:")) != -1)
		{	switch (option_char)
			{	case 'a':
					averaging = false;
					break;
				case 'd':
					domain_name = optarg;
					break;
				case 'e':
					num_episodes = from_string<unsigned>(optarg);
					break;
				case 'h':
					cout << usage(argv[0]);
					return 0;
				case 'l':
					learner_name = optarg;
					break;
				case 'm':
					model_directory = optarg;
					break;
				case 'n':
					num_trajectories = from_string<unsigned>(optarg);
					break;
				case 'o':
					run_index = from_string<unsigned>(optarg);
					break;
				case 'r':
					num_runs = from_string<unsigned>(optarg);
					break;
				case 's':
					num_steps = from_string<unsigned>(optarg);
					break;
				case 't':
					trajectory_filename = optarg;
					break;
				case 'z':
					rand_seed(from_string<unsigned>(optarg));
					break;
				case '?':
					throw HierException(__FILE__, __LINE__, "Unknown command-line option or argument missing.");
			}
		}

		unique_ptr<MDP> mdp;
		if (domain_name.substr(0,4) == "taxi")
		{	if (domain_name.substr(4,3) == "_mp")
				mdp.reset(new Taxi_MP(domain_name, (domain_name.substr(7) == "_stoc" ? 0.8 : 1.0)));
			else if (domain_name.substr(4,3) == "_da")
				mdp.reset(new Taxi_DA(domain_name, (domain_name.substr(7) == "_stoc" ? 0.8 : 1.0)));
			else if (domain_name.substr(4,6) == "_mp_da")
				mdp.reset(new Taxi_MP_DA(domain_name, (domain_name.substr(10) == "_stoc" ? 0.8 : 1.0)));
			else
				mdp.reset(new Taxi(domain_name, (domain_name.substr(4) == "_stoc" ? 0.6 : 1.0)));
		}
		else if (domain_name.substr(0,7) == "cleanup")
		{
			mdp.reset(new Cleanup(domain_name, (domain_name.substr(4) == "_stoc" ? 0.6 : 1.0)));
		}
		else if (domain_name.substr(0,7) == "bitflip")
		{	if (optind >= argc)
				throw HierException(__FILE__, __LINE__, "Insufficient arguments for bitflip: num_bits.");
			mdp.reset(new Bitflip(domain_name, from_string<int>(argv[optind])));
		}
		else if (domain_name.substr(0,6) == "wargus")
		{	if (optind + 8 >= argc)
				throw HierException(__FILE__, __LINE__, "Insufficient arguments for wargus: port(default: 4870) source_map_name num_source_maps source_gold_quota source_wood_quota"
																												"target_map_name num_target_maps target_gold_quota target_wood_quota.");

			if (domain_name.substr(6) == "_goto")
				mdp.reset(new Wargus_Goto(domain_name, new ClientSocket("localhost", from_string<int>(argv[optind])),
										argv[optind + 1], from_string<unsigned>(argv[optind + 2]), Wargus_State::Resources(from_string<int>(argv[optind + 3]), from_string<int>(argv[optind + 4])),   // Source
										argv[optind + 5], from_string<unsigned>(argv[optind + 6]), Wargus_State::Resources(from_string<int>(argv[optind + 7]), from_string<int>(argv[optind + 8])),   // Target
										-1, display ? 1 : -1));
			else if (domain_name.substr(6) == "_nsew")
				mdp.reset(new Wargus_NSEW(domain_name, new ClientSocket("localhost", from_string<int>(argv[optind])),
										argv[optind + 1], from_string<unsigned>(argv[optind + 2]), Wargus_State::Resources(from_string<int>(argv[optind + 3]), from_string<int>(argv[optind + 4])),   // Source
										argv[optind + 5], from_string<unsigned>(argv[optind + 6]), Wargus_State::Resources(from_string<int>(argv[optind + 7]), from_string<int>(argv[optind + 8])),   // Target
										-1, display ? 1 : -1));
			else
				throw HierException(__FILE__, __LINE__, "Unknown wargus MDP name.");
		}
		else
			throw HierException(__FILE__, __LINE__, "Incorrect program arguments.  Please look at the usage information below.\n\n" + usage(argv[0]));

		Simulator simulator;
		if (num_trajectories && !trajectory_filename.empty())
			simulator.trajectory_generator(trajectory_filename, *mdp, nullptr, num_trajectories);
		else
		{	unique_ptr<Learner> learner;   // A star is born
			if (learner_name == "maxq" || learner_name == "hh")
			{	unique_ptr<Hierarchy> hierarchy;
				if (num_trajectories && trajectory_filename.empty())
					hierarchy.reset(new Hierarchy(simulator.trajectory_generator("", *mdp, nullptr, num_trajectories), model_directory, run_index, *mdp));
				else if (!trajectory_filename.empty())
					hierarchy.reset(new Hierarchy(trajectory_filename, model_directory, *mdp));

				if (learner_name == "maxq")
					learner.reset(new MaxQ(learner_name, *mdp, hierarchy, run_index));
				else
					learner.reset(new HH(learner_name, *mdp, hierarchy, run_index));
			}
			else if (learner_name == "q" || learner_name == "h")
			{	if (!model_directory.empty() || num_trajectories || !trajectory_filename.empty())
					cerr << "Ignoring HierGen options\n";

				if (learner_name == "q")
					learner.reset(new Q(learner_name, *mdp));
				else
					learner.reset(new H(learner_name, *mdp));
			}
			else
				throw HierException(__FILE__, __LINE__, "Incorrect program arguments.  Please look at the usage information below.\n\n" + usage(argv[0]));

			if (num_runs && (num_episodes || num_steps))
			{	create_directory("results");

				if (num_episodes)
					simulator.episode(*mdp, *learner, num_runs, run_index, num_episodes, averaging);
				else
					simulator.step(*mdp, *learner, num_runs, run_index, num_steps, averaging);

				learner->write_text_file("results/learner_" + learner->name() + "_" + mdp->name() + ".out", *mdp);
			}
		}
	}
	catch (const exception& e)
	{	cerr << e.what() << endl << endl;
	}
	catch (...)
	{	cerr << "\n Unhandled exception.\n";
	}

	return 0;
}
