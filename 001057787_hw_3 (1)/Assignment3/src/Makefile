#
#	makefile for GSM Enhanced Full Rate Speech Codec
#


include Makefile.macros


COMMON = $(COMMONDIR)


SCCOPTS += -P./lp_analysis -P./open_loop -P./closed_loop \
           -P./codebook -P./update -P./processing -P./decoder

#SCCOPTS = $(SCCVERB) $(SCCWARN) $(SCCFLAGS)



# targets

all:	testbench


test:	testbench
	if echo "$(DEFINES)" | grep -q ENABLE_DECODER ; then	\
	  ./tst_vocoder ;					\
	else							\
	  ./tst_coder ;						\
	fi

stest:	testbench
	./tst_silent


tools:	bit2cod bitcat


tar tarball: 
	$(TAR) cvzfT $(PACKAGE) $(FILES)





# Explicit Rules

testbench: testbench.sc
	$(SCC) testbench $(SCCOPTS) -xl $(COMMON)/pack.$(OBJSFX) $(SCCLIBS)

testbench.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh \
               coder.sc stimulus.sc monitor.sc arg_handler.sc

stimulus.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/std_includes.sc

monitor.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/std_includes.sc

arg_handler.sc: $(COMMON)/typedef.sh $(COMMON)/std_includes.sc

coder.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc cod_12k2.sc \
	   processing/pre_process.sc processing/post_process.sc

cod_12k2.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc lp_analysis/lp_analysis.sc \
              open_loop/open_loop.sc subframes.sc update/shift_signals.sc

subframes.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/array_op.sc $(COMMON)/reset.sc \
                 closed_loop/closed_loop.sc codebook/codebook_cn.sc update/update.sc


bit2cod: bit2cod.sc
	$(SCC) bit2cod $(SCCOPTS)

bitcat: bitcat.sc
	$(SCC) bitcat $(SCCOPTS)


clean:
	cd common; $(MAKE) clean;
	cd lp_analysis; $(MAKE) clean;
	cd open_loop; $(MAKE) clean;
	cd closed_loop; $(MAKE) clean;
	cd codebook; $(MAKE) clean;
	cd update; $(MAKE) clean;
	cd processing; $(MAKE) clean;
	cd decoder; $(MAKE) clean;
	$(RM) *.bak *.BAK *~
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) libEFR_Coder.so
	$(RM) testbench$(EXESFX)
	$(RM) bit2cod
	$(RM) bitcat
	$(RM) core
	$(RM) dtx.bit nodtx.bit speech.out
