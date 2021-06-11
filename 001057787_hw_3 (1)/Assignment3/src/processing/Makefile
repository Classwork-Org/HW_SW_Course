#
#	makefile for pre and post processing
#


include ../Makefile.macros





all:    pre_process.$(OBJSFX) post_process.$(OBJSFX) post_filter.$(OBJSFX) bits2prm.$(OBJSFX)


# Explicit Rules

pre_process.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc homing_test.sc filter_and_scale.sc

homing_test.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh 

filter_and_scale.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc $(COMMON)/reset.sc

post_process.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc prm2bits.sc cn_encoding.sc sid_codeword_encode.sc

prm2bits.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/basic_op.sc

cn_encoding.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh $(COMMON)/reset.sc

sid_codeword_encode.sc: $(COMMON)/typedef.sh $(COMMON)/cnst.sh 


clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
