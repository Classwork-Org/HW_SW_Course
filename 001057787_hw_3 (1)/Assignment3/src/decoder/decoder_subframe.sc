
#include "cnst.sh"
#include "typedef.sh"

import "dec_lag_code";
import "pred_lt_6";
import "d_gain_pitch";
import "add_pitch_contr";
import "d_gain_code";
import "synth_speech";

import "reset";


    /*---------------------------------------------------------------------*
     *       Loop for every subframe in the analysis frame                 *
     *---------------------------------------------------------------------*
     * The subframe size is L_SUBFR and the loop is repeated               *
     * L_FRAME/L_SUBFR times                                               *
     *     - decode the pitch delay                                        *
     *     - decode algebraic code                                         *
     *     - decode pitch and codebook gains                               *
     *     - find the excitation and compute synthesis speech              *
     *---------------------------------------------------------------------*/

behavior Decoder_Subframe(in  Word16 state,
                          in  Word16 i_subfr,
                          in  Word16 bfi,
                          in  Word16 prev_bf,
                          in  Word16 Az[MP1],
		              Word16 *parm,
		              Word16 *exc,
                          out Word16 synth[L_SUBFR],
                          in  Word16 rxdtx_ctrl,
                          in  Word16 rx_dtx_state,
			      Word32 L_pn_seed_rx)
  implements Ireset
{
  Word16 index;
  
  Word16 *p_exc_i;
  Word16 exc_i[L_SUBFR];
  Word16 excp[L_SUBFR];

  Word16 T0, T0_frac;
  
  Word16 code[L_SUBFR];

  Word16 pit_sharp;
  Word16 gain_pitch;
  Word16 gain_code;
  
  
  Dec_lag_code_cn dec_lag_code(rxdtx_ctrl, i_subfr, L_pn_seed_rx,
			       parm, bfi, T0_frac, T0, code);
  Pred_Lt_6 pred_lt_6(p_exc_i, T0, T0_frac);
  D_gain_pitch d_gain_pitch(index, bfi, state, prev_bf, rxdtx_ctrl, gain_pitch);

  Add_pitch_contr add_pitch_contr(gain_pitch, T0, exc_i, code, excp, pit_sharp);

  D_gain_code d_gain_code(index, code, gain_code, bfi, state, prev_bf,
			  rxdtx_ctrl, i_subfr, rx_dtx_state);

  Synth_speech synth_speech(pit_sharp, gain_pitch, gain_code, Az, code, exc_i, excp,
			    synth);
  
  void reset(void)
  {
    dec_lag_code.reset();
    d_gain_pitch.reset();
    d_gain_code.reset();
    synth_speech.reset();
  }
  
  void main(void)
  {
    p_exc_i = &exc[i_subfr];
    
    dec_lag_code.main();
    if ((rxdtx_ctrl & RX_SP_FLAG) != 0) {
      pred_lt_6.main();
    }
    index = parm[1];
    d_gain_pitch.main();
    parm += 12;
        
    add_pitch_contr.main();
    
    index = *parm++;                /* index of energy VQ */
    d_gain_code.main();

    Copy(p_exc_i, exc_i, L_SUBFR);
    synth_speech.main();
    Copy(exc_i, p_exc_i, L_SUBFR);
  }
};

