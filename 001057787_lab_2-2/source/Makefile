# --- macros -------------------------------------------------------------
ALL				 = canny
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

all:	$(ALL)
	mv $(ALL) $(BIN_DIR)/

test:
	$(BIN_DIR)/canny $(IMG_DIR)/$(TEST_IMG)
	eog $(IMG_DIR)/$(TEST_IMG)_s_$(sigma)_l_$(tlow)_h_$(thigh).pgm;

clean:
	-$(RM) *.bak *.BAK
	-$(RM) *.si *.sir *.cc *.h *.o
	-$(RM) $(ALL) *.exe
	-$(RM) $(BIN_DIR)/*
	find $(IMG_DIR)/*.pgm*.pgm | xargs -t -I % $(RM) %
