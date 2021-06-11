#include "typedef.sh"
#include "cnst.sh"


import "copy";

import "codebook";
import "build_cn_code";



behavior Codebook_CN(
		     in  Word16 xn[L_SUBFR],
		     in  Word16 y1[L_SUBFR],
		     in  Word16 gain_pit,
		     in  Word16 exc[L_SUBFR],
		     in  Word16 h1[L_SUBFR],
		     in  Word16 T0,
		     in  Word16 res2[L_SUBFR],
		         Word16 code[L_SUBFR],
		         Word16 y2[L_SUBFR],
		     out Word16 gain_code,
		     out Word16 ana[10],
		     in  DTXctrl txdtx_ctrl,
		         Word32 L_pn_seed_tx
		     )
  
{
  Nop      nop;
  
  Codebook codebook(xn, y1, gain_pit, exc, h1, T0, res2, code, y2, 
		    gain_code, ana);
  Build_CN_Code build_cn_code(code, L_pn_seed_tx);

  void main(void)
  {
    fsm
    {
      nop: {
	if ((txdtx_ctrl & TX_SP_FLAG) != 0) goto codebook;
	goto build_cn_code;
      }

      codebook: {
	break;
      }

      build_cn_code:
    }
  }
};
