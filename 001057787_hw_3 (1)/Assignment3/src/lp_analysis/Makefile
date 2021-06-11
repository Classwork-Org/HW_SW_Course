#
#	makefile for linear prediction analysis
#

include ../Makefile.macros



all:    lp_analysis.$(OBJSFX)


# Explicit Rules

lp_analysis.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/array_op.sc $(COMMON)/reset.sc find_az.sc \
                 az_lsp.sc vad_lp.sc int_lpc.sc q_plsf_and_intlpc.sc no_speech_upd.sc lp_analysis.tab

find_az.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh autocorr.sc lag_wind.sc levinson.sc

autocorr.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

lag_wind.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

levinson.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

az_lsp.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

vad_lp.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc $(COMMON)/copy.sc vad_comp.sc tx_dtx.sc nodtx_setflags.sc

vad_comp.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/reset.sc

tx_dtx.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/reset.sc

nodtx_setflags.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh

q_plsf_and_intlpc.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc int_lpc.sc q_plsf_5.sc

int_lpc.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/lsp_az.sc

q_plsf_5.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/reset.sc q_plsf_5.tab

no_speech_upd.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh 


clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
