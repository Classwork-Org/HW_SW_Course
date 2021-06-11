# programs
include ../Makefile.macros

SCC = scc
RM = rm -f

# parameters

SCCVERB = -vv
SCCWARN = -ww
SCCFLAGS = -g 

SCCOPTS = $(SCCVERB) $(SCCWARN) $(SCCFLAGS)




# rules

.SUFFIXES:
.SUFFIXES: .sc .sir

.sc.sir:
	$(SCC) $* -sc2sir $(SCCOPTS)




# targets

all:	pub_files testbench

test:	testbench
	LD_LIBRARY_PATH=$${LD_LIBRARY_PATH}$${LD_LIBRARY_PATH:+:}. ./tst_IP

pub_files:
	+-ln -s ../EFR_Coder_public.sc . 2>/dev/null
	+-ln -s ../EFR_Coder_public.sh . 2>/dev/null
	+-ln -s ../common/reset.sc . 2>/dev/null
	+-ln -s ../libEFR_Coder.so .	2>/dev/null



testbench: testbench.$(OBJSFX)
	$(SCC) testbench -o2out $(SCCOPTS) \
	-L. -lEFR_Coder


testbench.$(OBJSFX): testbench.sir
	$(SCC) testbench -sc2o $(SCCOPTS)

testbench.sir: EFR_Coder_public.sh EFR_Coder_public.sir \
               stimulus.sir monitor.sir arg_handler.sir

EFR_Coder_public.sir: EFR_Coder_public.sh EFR_Coder_public.sc reset.sir

stimulus.sir arg_handler.sir monitor.sir: EFR_Coder_public.sh std_includes.sir



clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h *.$(OBJSFX)
	$(RM) testbench
	$(RM) core *.bak *.BAK *~
	$(RM) EFR_Coder_public.sh
	$(RM) EFR_Coder_public.sc
	$(RM) libEFR_Coder_sim.a
