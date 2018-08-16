.PHONY: hierlearning clean
CC = g++
FLAGS = -Wall -O2 -DNDEBUG -std=c++0x

EXE = hierlearning
DOMAINS = bitflip taxi wargus
LEARNERS = flat/generic flat/q flat/h hier/generic hier/hh hier/maxq

FILES :=
include $(patsubst %, domain/%/module.make, $(DOMAINS))
include $(patsubst %, learner/%/module.make, $(LEARNERS))
include hiergen/module.make
FILES := $(FILES) lib/common.o simulator.o main.o

%.o: %.cc
	$(CC) $(FLAGS) -c $< -o $@

hierlearning: $(FILES)
	$(CC) $(FLAGS) -o $(EXE) $(FILES)

clean:
	rm -f $(FILES)
