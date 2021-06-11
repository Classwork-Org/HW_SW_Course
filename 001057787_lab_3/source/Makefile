# --- macros -------------------------------------------------------------
SRC				 = canny
SCC				 = scc
DEBUG 			 = gdb
DEBUGOPT 		 = --args
SCCOPT			 = -vv -www -g -d -G
RM				 = rm -f
sigma			 = 0.60
tlow			 = 0.30
thigh			 = 0.80
IMG_DIR			 = ../img
BIN_DIR			 = ../bin
TEST_IMG		 = beachbus.pgm
REF_IMG			 = beachbus-ref-edge.pgm

# --- SpecC rules --------------------------------------------------------

.SUFFIXES:
.SUFFIXES:	.sc .cc .o .c

.sc.cc:
	$(SCC) $* -sc2cch $(SCCOPT)

.cc.o:
	$(SCC) $* -cc2o $(SCCOPT)

.o:
	$(SCC) $* -o2out $(SCCOPT)

.cc:
	$(SCC) $* -cc2out $(SCCOPT)

.sc:
	$(SCC) $* -sc2out $(SCCOPT)

# --- targets ------------------------------------------------------------

all:	$(SRC)
	mv $(SRC) $(BIN_DIR)/

test:
	$(BIN_DIR)/$(SRC) $(IMG_DIR)/$(TEST_IMG) $(IMG_DIR)/$(REF_IMG) $(IMG_DIR)/beachbus-out.pgm
	eog $(IMG_DIR)/$(IMG_DIR)/beachbus-out.pgm;

clean:
	$(RM) $(BIN_DIR)/$(SRC)
	$(RM) *.si *.sir *.cc *.h *.o
	$(RM) $(IMG_DIR)/*-out.pgm
