FOLDER = learner/hier/generic
OBJ = task.o composite_task.o sequenced_task.o hier_learner.o
FILES += $(addprefix $(FOLDER)/, $(OBJ))
