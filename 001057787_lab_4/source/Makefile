# ------------------------------------------------------------------------
# Makefile for simple examples
# ------------------------------------------------------------------------
#
# Modifications: (most recent first)
#

# --- macros -------------------------------------------------------------

# what to compile by default? 
ALL	= canny_single
# compiler 
SCC	= scc
# compile options 
SCCOPT	= -vv -w -g -G -Tvcds
RM	= rm -f

FILE_IN =  ../img/beachbus.pgm
FILE_OUT = ../img/beachbus-edges.pgm
#FILE_REF = beachbus-edges.golden.pgm
# compare first against the original 
FILE_REF =  ../img/beachbus.pgm





# --- SpecC rules --------------------------------------------------------

# pattern rules for compilation 

# known patterns
.SUFFIXES:
.SUFFIXES:	.sc .cc .o


.sc.cc:
	$(SCC) $* -sc2cch $(SCCOPT)

.cc.o:
	$(SCC) $* -cc2o $(SCCOPT)

.o:
	$(SCC) $* -o2out $(SCCOPT)

.cc:
	$(SCC) $* -cc2out $(SCCOPT)

# from *.sc to * i.e. compilation to executable
.sc:
	$(SCC) $* -sc2out $(SCCOPT)


# --- targets ------------------------------------------------------------

# default rule
all: $(ALL)

# test: all
# 	./canny_single
# 	gtkwave canny_single.vcd


clean:
	-$(RM) *.bak *.BAK
	-$(RM) *.si *.sir *.cc *.h *.o
	-$(RM) $(ALL) $(FILE_OUT)


# specific dependency for canny_single (indluces defs.sh)	
canny_single: defs.sh


# test only for single target
ifneq ($(words $(ALL)),1)
$(error Test rule only works for single target)
endif

# specific rule for testing canny 
$(FILE_OUT): $(ALL) $(FILE_IN)
	./$(ALL) $(FILE_IN)

# make test depend on output file and ref file
# this way execution is only done when needed 
test: $(FILE_OUT) $(FILE_REF)
	diff -q $(FILE_OUT) $(FILE_REF)

	
# example rule for multiple targets
#test:	$(ALL)
#	set -e;					\
#	for file in $(ALL); do ./$$file ; done


# --- EOF ----------------------------------------------------------------
