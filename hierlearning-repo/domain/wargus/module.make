FOLDER = domain/wargus
OBJ = client/socket.o wargus.o wargus_goto.o wargus_nsew.o
FILES += $(addprefix $(FOLDER)/, $(OBJ))
