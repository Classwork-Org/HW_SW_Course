
#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "basic_op";
import "basic_func";

import "d_plsf_5";
import "int_lpc";


behavior Decode_Lsp_update(    Word16 *parm,
			   in  Word16 rxdtx_ctrl,
			   in  Word16 lsp_new[M],
			   out Word16 lsp_old[M],
			   out Word16 A_t[4][MP1])

{
  void main(void)
  {
    int i;
    Word16 A_tmp[MP1];
    
    parm += 5;
    
    if ((rxdtx_ctrl & RX_SP_FLAG) == 0)
    {
        /* Comfort noise: use the same parameters in each subframe */
        Lsp_Az (lsp_new, A_tmp);

        A_t[0] = A_tmp;
        A_t[1] = A_tmp;
        A_t[2] = A_tmp;
        A_t[3] = A_tmp;      
    }

    /* update the LSPs for the next frame */
    for (i = 0; i < M; i++)
    {
        lsp_old[i] = lsp_new[i];
    }
    
  }
};



behavior Decode_Lsp(    Word16 *parm,
		    in  Word16 bfi,
		    in  Word16 rxdtx_ctrl,
		    in  Word16 rx_dtx_state,
		    out Word16 A_t[4][MP1])
  implements Ireset
{
  Word16 lsp_old[M];
  Word16 lsp_new[M];
  Word16 lsp_mid[M];
  
  
  D_plsf_5 d_plsf_5(parm, lsp_mid, lsp_new, bfi, rxdtx_ctrl, rx_dtx_state);
  Int_Lpc  int_lpc(lsp_old, lsp_mid, lsp_new, A_t);
  Decode_Lsp_update update(parm, rxdtx_ctrl, lsp_new, lsp_old, A_t);

  void init(void)
  {
    /* Initialize lsp_old [] */

    lsp_old[0] = 30000;
    lsp_old[1] = 26000;
    lsp_old[2] = 21000;
    lsp_old[3] = 15000;
    lsp_old[4] = 8000;
    lsp_old[5] = 0;
    lsp_old[6] = -8000;
    lsp_old[7] = -15000;
    lsp_old[8] = -21000;
    lsp_old[9] = -26000;
  }
  
  void reset(void)
  {
    init();
    d_plsf_5.reset();
  }
  
  void main(void)
  {
    fsm 
    {
      d_plsf_5: {
	if ((rxdtx_ctrl & RX_SP_FLAG) == 0) goto update;
      }
      
      int_lpc: {
      }
      
      update: {
      }
    }
  }
};

      
