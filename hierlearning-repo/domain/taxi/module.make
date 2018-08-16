FOLDER = domain/taxi
OBJ = taxi.o taxi_mp.o taxi_da.o taxi_mp_da.o
FILES += $(addprefix $(FOLDER)/, $(OBJ))
