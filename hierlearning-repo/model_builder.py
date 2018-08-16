
# Model Builder
# Converts trajectories to arff files --- one for each action-variable pair --- and builds J48 decision-tree action models
# Compatible with Weka 3.6.5
# Note: numeric attributes are not allowed for J48


import sys
import subprocess
import re
import os
import platform
oper_sys = platform.system()

if len(sys.argv) != 5:
        print "Arguments: <trajectory filename> <output model directory> <confidence (0 = no pruning)> <display?>"
        sys.exit(1)

confidence = float(sys.argv[3])
prune = confidence != 0.0
display = sys.argv[4] == "True"

if not os.access(sys.argv[1], os.R_OK):
	print "Trajectory file cannot be read."
	sys.exit(1)

if not os.access(sys.argv[2], os.W_OK):
	os.mkdir(sys.argv[2])

infile = open(sys.argv[1], 'r')
data = infile.readlines();
infile.close()

pattern = re.compile(r'[ ]+')
actions = pattern.split(data[0].strip())
num_actions = len(actions)
action_encoding = dict(zip(map(int, pattern.split(data[1].strip())), [i for i in range(num_actions)]))

state_variables = pattern.split(data[2].strip())
num_variables = len(state_variables)
domain_size = map(int, pattern.split(data[3].strip()))

if num_variables != len(domain_size):
	print "The number of variables does not match the number of domain sizes."
	sys.exit(1)

directory = sys.argv[2] + '/'

if display:
        sys.stdout.write("Creating arff files ... ")
arff_file = []

# Transition data
for a in range(num_actions):
        arff_file.append([])
        for x in range(num_variables):
                arff_file[a].append(open(directory + actions[a] + '__' + state_variables[x] + '.arff', 'w'))
                arff_file[a][x].write('@relation ' + actions[a] + '__' + state_variables[x] + '\n')
                for v in range(num_variables):
                        arff_file[a][x].write('@attribute ' + state_variables[v] + ' {' + str(range(domain_size[v])).strip('[]') + '}\n')
                arff_file[a][x].write('@attribute next_' + state_variables[x] + ' {' + str(range(domain_size[x])).strip('[]') + '}\n\n@data\n')

reward_nominal_values = []
reward_data = []
for a in range(num_actions):
        reward_nominal_values.append(set())
        reward_data.append("")

pattern = re.compile(r'[ \(\),\*]+')
for i in range(5, len(data) - 3):
        state = [s for s in pattern.split(data[i].strip()) if s]
        if len(state) > 1:
                if len(state) != num_variables:
                        print "The state does not contain the expected number of state variables."
                        sys.exit(1)

                if data[i+1].strip() == '#':
                        continue
                a = action_encoding[int(data[i+1].strip())]
                reward = float(data[i+2].strip())
                next_state = [s for s in pattern.split(data[i+3].strip()) if s]

                # Transition arff
                for x in range(num_variables):
                        for s in state:
                                arff_file[a][x].write(s + ',')
                        arff_file[a][x].write(next_state[x] + '\n')

                # Reward arff
                for s in state:
                        reward_data[a] += s + ','
                reward_data[a] += str(reward) + '\n'
                reward_nominal_values[a].add(reward)

for a in range(num_actions):
        for x in range(num_variables):
                arff_file[a][x].close()

# Reward data
for a in range(num_actions):
        reward_arff_file = open(directory + actions[a] + '__reward.arff', 'w')
        reward_arff_file.write('@relation ' + actions[a] + '__reward\n')
        for v in range(num_variables):
                reward_arff_file.write('@attribute ' + state_variables[v] + ' {' + str(range(domain_size[v])).strip('[]') + '}\n')
        reward_arff_file.write('@attribute reward {' + str(reward_nominal_values[a]).strip('set()[]') + '}\n\n@data\n')
        reward_arff_file.write(reward_data[a])
        reward_arff_file.close()

if display:
        print "done."

# Process arff files
if display:
        print "Creating models ..."
line_pattern = re.compile(r'[\r\n]*')
count_pattern = re.compile(r' \(.*\)')
for a in range(num_actions):
        if display:
                print actions[a]

	for x in range(num_variables):
                if not prune:
        		model_str = subprocess.Popen(["java", "-cp", "C:/Users/JW/workspace/hierlearningAlt/lib/weka.jar", "weka.classifiers.trees.J48", "-v", "-no-cv", "-U", "-M", "1", "-t", directory + actions[a] + "__" + state_variables[x] + ".arff"], stdout = subprocess.PIPE).communicate()[0]
       		else:
        		model_str = subprocess.Popen(["java", "-cp", "C:/Users/JW/workspace/hierlearningAlt/lib/weka.jar", "weka.classifiers.trees.J48", "-v", "-no-cv", "-C", str(confidence), "-M", "1", "-t", directory + actions[a] + "__" + state_variables[x] + ".arff"], stdout = subprocess.PIPE).communicate()[0]

		if model_str != "":
			model = line_pattern.split(model_str)
			model_file = open(directory + actions[a] + '__' + state_variables[x] + '.model', 'w')
			for line in model[4:len(model) - 3]:
				if line.find(' (0.0)') == -1:
					model_file.write(count_pattern.split(line)[0] + '\n')
			model_file.close()

	if len(reward_nominal_values[a]) > 1:
                if not prune:
        		model_str = subprocess.Popen(["java", "-cp", "C:/Users/JW/workspace/hierlearningAlt/lib/weka.jar", "weka.classifiers.trees.J48", "-v", "-no-cv", "-U", "-M", "1", "-t", directory + actions[a] + "__reward.arff"], stdout = subprocess.PIPE).communicate()[0]
        	else:
        		model_str = subprocess.Popen(["java", "-cp", "C:/Users/JW/workspace/hierlearningAlt/lib/weka.jar", "weka.classifiers.trees.J48", "-v", "-no-cv", "-C", str(confidence), "-M", "1", "-t", directory + actions[a] + "__reward.arff"], stdout = subprocess.PIPE).communicate()[0]

                if model_str != "":
			model = line_pattern.split(model_str)
			model_file = open(directory + actions[a] + '__reward.model', 'w')
			for line in model[4:len(model) - 3]:
				if line.find(' (0.0)') == -1:
					model_file.write(count_pattern.split(line)[0] + '\n')
			model_file.close()
	else:
		model_file = open(directory + actions[a] + '__reward.model', 'w')
		model_file.write(': ' + str(reward_nominal_values[a]).strip('set()[]') + '\n')
		model_file.close()
if display:
        print "Done."
