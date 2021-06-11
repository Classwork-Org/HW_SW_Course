/*------------------------------------------------------*
 * - Find the total excitation                          *
 * - find synthesis speech corresponding to exc[]       *
 * - update filter memories for finding the target      *
 *   vector in the next subframe                        *
 *   (update mem_err[] and mem_w0[])                    *
 *------------------------------------------------------*/
//
// 01/08/02  <gerstl>	Updated to comply with extended port checking


#include "typedef.sh"
#include "cnst.sh"

import "reset";


import "q_gain_code";
import "ex_syn_upd_sh";






behavior Update (
 in  Int    i_subfr, 
 in  Word16 Aq[MP1],
 in  Word16 gain_pit,
 in  Word16 gain_code,
     Word16 exc_i[L_SUBFR],
 in  Word16 speech_i[L_SUBFR],
 out Word16 synth_i[L_SUBFR],
 out Word16 ana,
 in  Word16 xn[L_SUBFR],            /* Target vector for pitch search        */
 in  Word16 y1[L_SUBFR],
 in  Word16 y2[L_SUBFR],
 in  Word16 code[L_SUBFR],          /* (i): fixed codebook excitation       */
 out Word16 mem_err[M],
 out Word16 mem_w0[M],
 in  DTXctrl txdtx_ctrl,
 in  Word16 CN_excitation_gain,
 in  Flag   reset_flag		 
 )
implements Ireset
{
  Word16 qgain_code;

  Q_Gain_Code   q_gain_code(i_subfr, code, gain_code, qgain_code, txdtx_ctrl, 
                            CN_excitation_gain, ana, reset_flag);
  Ex_Syn_Upd_Sh ex_syn_upd_sh(gain_pit, qgain_code, exc_i, speech_i,
                              synth_i, xn, y1, y2, code, Aq, mem_err, mem_w0, 
                              txdtx_ctrl, reset_flag);


  void reset(void)
    {
      q_gain_code.reset();
      ex_syn_upd_sh.reset();
    }


  void main(void)
    {
      q_gain_code.main();
      ex_syn_upd_sh.main();
    }
};
