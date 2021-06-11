#	makefile for common files
#

include ../Makefile.macros





all:    std_includes.$(OBJSFX) reset.$(OBJSFX) basic_op.$(OBJSFX)	\
	F_gamma.$(OBJSFX) array_op.$(OBJSFX) copy.$(OBJSFX) 		\
	basic_func.$(OBJSFX) lsp_az.$(OBJSFX) residu.$(OBJSFX) 		\
	syn_filt.$(OBJSFX) weight_ai.$(OBJSFX) gains_tb.$(OBJSFX)	\
	channels.$(OBJSFX) pack.$(OBJSFX)



# Explicit Rules

basic_op.sc: typedef.sh cnst.sh std_includes.sc

F_gamma.sc: typedef.sh cnst.sh 

array_op.sc: typedef.sh 

copy.sc: typedef.sh cnst.sh

basic_func.sc: typedef.sh basic_op.sc

lsp_az.sc: typedef.sh basic_op.sc

residu.sc: typedef.sh cnst.sh basic_op.sc

syn_filt.sc: typedef.sh cnst.sh basic_op.sc

weight_ai.sc: typedef.sh cnst.sh basic_op.sc



clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
