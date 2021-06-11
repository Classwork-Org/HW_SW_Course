#include "cnst.sh"
#include "typedef.sh"


import "array_op";

import "residu";
import "syn_filt";
import "compute_CN_excitation_gain";


behavior Find_Targetvec_Copy1(
                              in  Word16 res2b[L_SUBFR],
                              in  Word16 mem_err[M],
                              out Word16 res2[L_SUBFR],
                              out Word16 *p_exc_i,
                              out Word16 mem[M]
			     )
{
  void main(void)
  {
    res2 = res2b;
    Copy (res2b, (Word16*)p_exc_i, L_SUBFR);
    Copy (mem_err, (Word16*)mem, M);                /* copy filter memory */
  }
};


behavior Find_Targetvec_Copy2(
                              in  Word16 mem_err[M],
                              in  Word16 error[L_SUBFR],
                                  Word16 mem_error[M + L_SUBFR],
                              out Word16 *p_error
			     )
{
  void main(void)
  {
    p_error = mem_error + M;
    Copy (mem_err, mem_error, M);          /* concatenate error */
    Copy (error, (Word16*)p_error, L_SUBFR);    
  }
};





behavior Find_Targetvec (
		 in  Word16 Aq[MP1],
		 in  Word16 Ap1[MP1],
		 in  Word16 Ap2[MP1],
		 in  Word16 *p_speech_i,
		 out Word16 res2[L_SUBFR],
		 out Word16 *p_exc_i,
		 out Word16 xn[L_SUBFR],
		 in  Word16 mem_err[M],
		     Word16 mem_w0[M],
		 in  DTXctrl txdtx_ctrl,       /* voice activity flags */
		 out Word16 CN_excitation_gain
		 )
{
  Word16 res2b[L_SUBFR];
  Word16 xn2[L_SUBFR];
  Word16 mem[M];
  Word16 error[L_SUBFR];
  Word16 mem_error[M + L_SUBFR];
  Word16 *p_error;

  Find_Targetvec_Copy1 copy1(res2b, mem_err, res2, p_exc_i, mem);
  Find_Targetvec_Copy2 copy2(mem_err, error, mem_error, p_error);
  
  Residu   residu_1(Aq, p_speech_i, res2b);
  Syn_Filt syn_filt_1(Aq, res2b, error, mem, false);
  Residu   residu_2(Ap1, p_error, xn2);
  Syn_Filt syn_filt_2(Ap2, xn2, xn, mem_w0, false);
  Compute_CN_Excitation_Gain 
    compute_CN_excitation_gain(res2b, CN_excitation_gain);
  
  void main(void)
  {
    fsm
    {
      residu_1:

      copy1: {
	if ((txdtx_ctrl & TX_SP_FLAG) == 0) goto compute_CN_excitation_gain;
	goto syn_filt_1;
      }

      /* Compute comfort noise excitation gain based on
       LP residual energy */
      compute_CN_excitation_gain: {
	break;
      }

      syn_filt_1:
      
      copy2:
      
      residu_2:
      syn_filt_2:
    }
      
  }
};
