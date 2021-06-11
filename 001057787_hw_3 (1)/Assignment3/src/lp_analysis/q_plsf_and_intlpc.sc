#include "typedef.sh"
#include "cnst.sh"


import "reset";

import "int_lpc";
import "q_plsf_5";



behavior Q_Plsf_And_Intlpc (
  out Word16 Aq_t[4][MP1],     /* A(z) quantized for the 4 subframes      */
  in  Word16 lsp_old[M],       /* old lsp[] (in case not found 10 roots)  */
  in  Word16 lsp_mid[M],       /* LSPs at 2nd subframe                    */
  in  Word16 lsp_new[M],       /* LSPs at 4th subframe                    */
      Word16 lsp_old_q[M],     /* old lsp[] (quantized)                   */
      Word16 lsp_mid_q[M],     /* LSPs at 2nd subframe (quantized)        */
      Word16 lsp_new_q[M],     /* LSPs at 4th subframe (quantized)        */
  out Word16 ana[PRM_SIZE],    /* quantization indices of 5 matrices      */
  in  DTXctrl txdtx_ctrl,      /* dtx control word                        */
  in  Flag reset_flag			    
  )
implements Ireset
{
  Q_Plsf_5 q_plsf_5(lsp_mid, lsp_new, lsp_mid_q, lsp_new_q, ana, txdtx_ctrl,
		    reset_flag);
  Int_Lpc int_lpc(lsp_old_q, lsp_mid_q, lsp_new_q, Aq_t);


  void reset(void)
    {
      q_plsf_5.reset();
    }
  

  void main(void)
    {
      fsm
	{
	  q_plsf_5: {
	    if ((txdtx_ctrl & TX_SP_FLAG) == 0) break;
	    /* goto int_lpc.main */
	  }
	  
	  int_lpc: {
	    /* goto update_lsps */
	  }
	  	  
	} /* msf */

    }
};
