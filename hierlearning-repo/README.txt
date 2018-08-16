HierLearning is a C++11 implementation of a general-purpose, multi-agent, hierarchical reinforcement learning system for sequential decision problems.  It was created as a platform for HierGen, an algorithm for hierarchical structure discovery in sequential decision problems.  For details, please refer to:
	Neville Mehta. Hierarchical Structure Discovery and Transfer in Sequential Decision Problems. Ph.D thesis, Oregon State University, 2011.


----------
 Features
----------

* Facilitates the implementation of hierarchical and non-hierarchical learning algorithms.
* Incorporates multi-agent learning.
* Facilitates the implementation of sequential decision problems.


--------------
 Requirements
--------------

(The versions that HierLearning has been verified on are mentioned in parentheses.)

* Compiler: Visual Studio (2012, v11) or gcc (v4.8.1)
* Weka (v3.6.5)
* Python (v2.7)

Optional:
* Graphviz (v2.28)
* Wargus (v2.1)
* Octave (v3.2.4)


--------------
 Installation
--------------

To build binary:
	make

To clean:
	make clean


-------
 Usage
-------

	hierlearning -h
	hierlearning -d <domain> -l <learner> [-r <number of runs> -e <number of episodes>]
	hierlearning -d <domain> -n <number of trajectories> -t <trajectory filename>
	hierlearning -d <domain> -l <learner> -n <number of trajectories> [-m <model directory>] [-r <number of runs> -e <number of episodes>]
	hierlearning -d <domain> -l <learner> -t <trajectory file> [-m <model directory>] [-r <number of runs> -e <number of episodes>]


----------
 Examples
----------

To load the manually-designed hierarchy and execute 10 runs of 100 episodes each:
	hierlearning -d taxi -l maxq -r 10 -e 100

To generate 50 random trajectories:
	hierlearning -d taxi -n 50 -t trajectory.out

To read the trajectory file and generate the task hierarchy based on the supplied models:
	hierlearning -d taxi -l maxq -t trajectory.out -m models

To generate 50 random trajectories, build the task hierarchy, and execute 10 runs of 100 episodes each:
	hierlearning -d taxi -l maxq -n 50 -r 10 -e 100


-----------
 Execution
-----------

Run on a cluster using qsub:
	cluster <domain> <learner> <trajectories> <runs> <episodes>

Process the output (needs Octave):
	process_results <domain> <learner> <runs>
