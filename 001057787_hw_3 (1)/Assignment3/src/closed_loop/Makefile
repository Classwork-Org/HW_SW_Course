#
#	makefile for adaptive codebook search
#


include ../Makefile.macros




all:    closed_loop.$(OBJSFX)


# Explicit Rules

closed_loop.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc par_weight.sc imp_resp.sc find_targetvec.sc \
                   pitch_fr6.sc enc_lag6.sc pred_lt_6.sc convolve.sc g_pitch.sc q_gain_pitch.sc

par_weight.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/weight_ai.sc

imp_resp.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/array_op.sc $(COMMON)/syn_filt.sc $(COMMON)/reset.sc

find_targetvec.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/array_op.sc $(COMMON)/residu.sc \
                    $(COMMON)/syn_filt.sc compute_CN_excitation_gain.sc

compute_CN_excitation_gain.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

pitch_fr6.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc \
               $(COMMON)/basic_func.sc

enc_lag6.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

pred_lt_6.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

convolve.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_func.sc

g_pitch.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

q_gain_pitch.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc



clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
