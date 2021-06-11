
#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "build_cn_code";
import "dec_lag6";
import "dec_10i40_35bits";




behavior Dec_lag_code_setup(in  Word16* parm,
			    in  Word16 rxdtx_ctrl,
			    out Word16 index,
			    out Word16 *code_parm,
			    out Word16 T0)
{
  void main(void)
  {
    index = *parm;       /* pitch index */
    code_parm = &parm[2];
    T0 = L_SUBFR;
  }
};

behavior Dec_lag_code(in  Word16 index,
		      in  Word16 i_subfr,
		      in  Word16 *parm,
		      in  Word16 bfi,
		      out Word16 T0_frac,
		      out Word16 T0,
		      out Word16 code[L_SUBFR])
  implements Ireset
{
  
  Dec_lag6 dec_lag6(index, PIT_MIN, PIT_MAX, i_subfr, L_FRAME_BY2,
                    bfi, T0_frac, T0);
  dec_10i40_35bits d1035pf(parm, code);

  void reset(void)
  {
    dec_lag6.reset();
  }
  
  void main(void)
  {
    par {
      dec_lag6.main();
      d1035pf.main();
    }
  }
};

behavior Dec_lag_code_cn(in  Word16 rxdtx_ctrl,
			 in  Word16 i_subfr,
			     Word32 L_pn_seed_rx,
			 in  Word16 *parm,
			 in  Word16 bfi,
			 out Word16 T0_frac,
			 out Word16 T0,
			 out Word16 code[L_SUBFR])
  implements Ireset
{
  Word16 *code_parm;
  Word16 index;
  
  Dec_lag_code_setup setup(parm, rxdtx_ctrl, index, code_parm, T0);
  Dec_lag_code dec_lag_code(index, i_subfr, code_parm, bfi, T0_frac, T0, code);
  Build_CN_Code build_cn_code(code, L_pn_seed_rx);
  
  void reset(void)
  {
    dec_lag_code.reset();
  }
  
  void main(void)
  {
    fsm
    {
      setup: {
        if ((rxdtx_ctrl & RX_SP_FLAG) == 0) goto build_cn_code;
      }
      
      dec_lag_code: {
	break;
      }
      
      build_cn_code: {
      }
    }
  }
};

