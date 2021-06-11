#
#	makefile for innovative codebook search
#


include ../Makefile.macros




all:    codebook_cn.$(OBJSFX)



# Explicit Rules

codebook_cn.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh codebook.sc build_cn_code.sc


codebook.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh pitch_contr.sc \
              prefilter.sc code_10i40_35bits.sc g_code.sc

pitch_contr.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

prefilter.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

g_code.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

code_10i40_35bits.sc: $(COMMON)/typedef.sh q_p.sc cor_h_x.sc set_sign.sc \
                       cor_h.sc search_10i40.sc build_code.sc

q_p.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

cor_h_x.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

set_sign.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/basic_func.sc

cor_h.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/basic_func.sc

search_10i40.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

build_code.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc


build_cn_code.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc



clean:
	$(RM) *.sir *.si *.h *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
