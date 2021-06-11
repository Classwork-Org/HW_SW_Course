#
#	makefile for update
#

include ../Makefile.macros





all:    update.$(OBJSFX) shift_signals.$(OBJSFX)


# Explicit Rules

update.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc q_gain_code.sc ex_syn_upd_sh.sc

q_gain_code.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/basic_func.sc $(COMMON)/reset.sc

ex_syn_upd_sh.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/array_op.sc $(COMMON)/syn_filt.sc $(COMMON)/reset.sc excitation.sc upd_mem.sc

excitation.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

upd_mem.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc


shift_signals.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/array_op.sc


clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
