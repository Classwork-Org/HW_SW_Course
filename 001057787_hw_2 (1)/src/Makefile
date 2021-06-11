# Makefile  adapted from the specc examples.

# default targets
ALL	= pd_stdCh_queue

# commands and options
SCC	= scc
SCCOPT	= -vv -w -g -G -Tvcds
RM	= rm -f


# --- SpecC rules --------------------------------------------------------
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

.sc:
	$(SCC) $* -sc2out $(SCCOPT)


# --- targets ------------------------------------------------------------

all:	$(ALL)
	mv $(ALL) ../bin	

clean:
	-$(RM) *.bak *.BAK
	-$(RM) *.si *.sir *.cc *.h *.o
	-$(RM) $(ALL) *.exe
	-$(RM) ../bin/*

test:	
	../bin/$(ALL)
	mv $(ALL).vcd ../trace

wave:
	gtkwave ../trace/$(ALL).vcd ../trace/signal_view.gtkw