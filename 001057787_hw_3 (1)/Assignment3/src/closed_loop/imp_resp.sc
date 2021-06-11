#include "cnst.sh"
#include "typedef.sh"

import "reset";


import "array_op";
import "syn_filt";



behavior Imp_Resp_Init(
		       in  Flag   reset_flag,
		       out Word16 zero[M],
		       out Word16 ai_zero[L_SUBFR]
		       )
  implements Ireset
{
  void init(void)
    {
      Set_zero((Word16*)zero, M);
      Set_zero((Word16*)ai_zero, L_SUBFR);
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


behavior Imp_Resp_Seq1(
		       in  Word16 Ap1[MP1],
		       out Word16 ai_zero[L_SUBFR]
		       )
{
  void main(void)
  {
    Copy(Ap1, (Word16*)ai_zero, MP1);
  }
};



behavior Imp_Resp (
		   in  Word16 Aq[MP1],
		   in  Word16 Ap1[MP1],
		   in  Word16 Ap2[MP1],
		   out Word16 h1[L_SUBFR],
		   in  DTXctrl txdtx_ctrl,      /* voice activity flags */
		   in  Flag reset_flag
		   )
  implements Ireset

{
  Word16 h1b[L_SUBFR];
  Word16 zero[M];
  Word16 ai_zero[L_SUBFR];

  Imp_Resp_Init init(reset_flag, zero, ai_zero);
  Imp_Resp_Seq1 seq1(Ap1, ai_zero);
  
  Syn_Filt syn_filt_1(Aq, ai_zero, h1b, zero, false);
  Syn_Filt syn_filt_2(Ap2, h1b, h1, zero, false);



  void reset(void)
    {
      init.reset();
    }
 

  void main(void)
  {
    fsm
    {
      init: {
	if ((txdtx_ctrl & TX_SP_FLAG) != 0) goto seq1;
	break;
      }
       
      seq1:
	  
      syn_filt_1:
      syn_filt_2:
	  
    }
  }
};
