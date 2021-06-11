
#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "array_op";  // for Set_zero()

import "F_gamma";

import "weight_ai";
import "residu";
import "syn_filt";
import "ol_lag_est";



behavior Open_Loop_Init(out Int i, in Flag reset_flag, Word16 mem_w[M])
  implements Ireset
{
  void init(void)
    {
      Set_zero(mem_w, M);
    }
  
  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    if (reset_flag) init();
    
    i = 0;
  }
};


behavior Open_Loop_Body1(
			 in  Word16 A_t[4][MP1],
			 in  Word16* p_speech,
			 in  Int i,
			 out Word16 A_t_i[MP1],
			 out Word16* p_speech_i
			 )
{
  void main(void)
  {
    A_t_i = A_t[i];
    p_speech_i = p_speech + i * L_SUBFR;
  }
};


behavior Open_Loop_Body2 (
                          in  Word16 A_t_i[MP1],
			  out Word16 ap1[MP1],
			  out Word16 ap2[MP1]
			  )
{
  Weight_Ai weight_ai_1(A_t_i, F_gamma1, ap1);
  Weight_Ai weight_ai_2(A_t_i, F_gamma2, ap2);

  void main(void)
  {
    par {
      weight_ai_1.main();
      weight_ai_2.main();
    }
  }
};


behavior Open_Loop_End(
                           Int i, 
                       in  Word16 wsp_i[L_SUBFR],
                       out Word16 *wsp
                       )
{
  void main(void)
  {
    Copy(wsp_i, (Word16*)wsp + i * L_SUBFR, L_SUBFR);    
    i++;
  }
};



behavior Open_Loop(
   in  Word16 *p_speech,
   in  Word16 A_t[4][MP1],
       Word16 *wsp,        /* (i/o)  this is the weighted speech */
   out Word16 T0_min_1,   /* range for closed loop pitch search subframes 1&2*/
   out Word16 T0_max_1,
   out Word16 T0_min_2,   /* range for closed loop pitch search subframes 3&4*/
   out Word16 T0_max_2,
   out Flag   ptch,         /* flag to indicate a periodic signal component */
   in  DTXctrl txdtx_ctrl, /* voice activity flags */
   in  Flag   dtx_mode,
   in  Flag   reset_flag		   
   )
implements Ireset
{
  Int i;
  
  Word16 mem_w[M];

  Word16 ap1[MP1];
  Word16 ap2[MP1];
  Word16 A_t_i[MP1];
  Word16 *p_speech_i;
  Word16 wsp_i[L_SUBFR];

  Open_Loop_Init  for_init(i, reset_flag, mem_w);
  
  Open_Loop_Body1 for_body1(A_t, p_speech, i, A_t_i, p_speech_i);
  Open_Loop_Body2 for_body2(A_t_i, ap1, ap2);
  
  Residu          residual(ap1, p_speech_i, wsp_i);
  Syn_Filt        syn_filter(ap2, wsp_i, wsp_i, mem_w, true);
  
  Open_Loop_End   for_end(i, wsp_i, wsp);
  
  Ol_Lag_Est ol_lag_estimate(wsp, T0_min_1, T0_max_1, T0_min_2, T0_max_2, 
			     ptch, txdtx_ctrl, dtx_mode, reset_flag);



  void reset(void)
    {
      for_init.reset();
      ol_lag_estimate.reset();
    }
 

  void main(void)
  {
    fsm 
    {
      for_init: {
	if (i <= 3) goto for_body1;
        goto ol_lag_estimate;
      }
      
      for_body1:
      
      for_body2:
      
      residual:
      
      syn_filter:
      
      for_end: {
	if (i <= 3) goto for_body1;
	goto ol_lag_estimate;
      }
      
      ol_lag_estimate:
    }
  }
};
