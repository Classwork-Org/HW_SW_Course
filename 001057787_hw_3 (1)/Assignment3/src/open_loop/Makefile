#
#	makefile for open loop pitch analysis
#

include ../Makefile.macros




all:    open_loop.$(OBJSFX)



# Explicit Rules

open_loop.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/array_op.sc $(COMMON)/weight_ai.sc \
               $(COMMON)/residu.sc $(COMMON)/syn_filt.sc $(COMMON)/reset.sc ol_lag_est.sc

ol_lag_est.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc pitch_ol.sc period_upd.sc get_minmax.sc

pitch_ol.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/basic_func.sc

period_upd.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/reset.sc

get_minmax.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc



clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
