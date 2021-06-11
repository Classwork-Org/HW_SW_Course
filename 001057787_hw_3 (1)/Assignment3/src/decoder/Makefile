#
#	makefile for pre and post processing
#


include ../Makefile.macros

SCCOPTS += -P../lp_analysis -P../codebook -P../closed_loop -P../processing


all: dec_12k2.$(OBJSFX)


# Explicit Rules

dec_12k2.sc: rx_dtx.sc decode_lsp.sc decoder_subframe.sc

decode_lsp.sc: d_plsf_5.sc

decoder_subframe.sc: dec_lag_code.sc d_gain_pitch.sc d_gain_code.sc \
                       add_pitch_contr.sc synth_speech.sc

dec_lag_code.sc: dec_lag6.sc dec_10i40_35bits.sc

clean:
	$(RM) *.sir *.si *.cc
	$(RM) *.h
	$(RM) *.$(OBJSFX)
	$(RM) core *.bak *.BAK *~
