//
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "typedef.sh"
#include "cnst.sh"

import "reset";

import "array_op";
import "copy";

import "excitation";
import "syn_filt";
import "upd_mem";


behavior Ex_Syn_Upd_Sh_Init(
			    in  Flag reset_flag,
			    out Word16 mem_syn[M]
			    )
  implements Ireset
{
  
  void init(void)
  {
    Set_zero((Word16*)mem_syn, M);
  }

  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    if (reset_flag) init();
  }
};



behavior Ex_Syn_Upd_Sh (
     in  Word16 gain_pit,
     in  Word16 gain_code,
         Word16 exc_i[L_SUBFR],
     in  Word16 speech_i[L_SUBFR],
     out Word16 synth_i[L_SUBFR],
     in  Word16 xn[L_SUBFR],            /* Target vector for pitch search   */
     in  Word16 y1[L_SUBFR],
     in  Word16 y2[L_SUBFR],
     in  Word16 code[L_SUBFR],      /* (i)      : fixed codebook excitation */
     in  Word16 Aq[MP1],
     out Word16 mem_err[M],
     out Word16 mem_w0[M],
     in  DTXctrl txdtx_ctrl,
     in  Flag reset_flag			
     )
implements Ireset
{
  Word16 mem_syn[M];

  Word16 synth[L_SUBFR];
  
  Ex_Syn_Upd_Sh_Init init(reset_flag,  mem_syn);
  
  Excitation excitation(gain_pit, gain_code, code, exc_i);
  Syn_Filt   syn_filt(Aq, exc_i, synth, mem_syn, true);
  Upd_Mem    upd_mem(speech_i, synth, xn, y1, y2, gain_pit, gain_code, 
		     mem_err, mem_w0, txdtx_ctrl);

  CopySubfr  copy(synth, synth_i);
  

  void reset(void)
  {
      init.reset();
  }


  void main(void)
  {
      init.main();

      excitation.main();
      
      syn_filt.main();
      upd_mem.main();
      
      copy.main();
    }
};
