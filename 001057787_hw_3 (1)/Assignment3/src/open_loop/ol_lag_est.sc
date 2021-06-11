#include "typedef.sh"
#include "cnst.sh"

import "reset";

import "pitch_ol";
import "period_upd";
import "get_minmax";



behavior Ol_Lag_Est_Seq1(in Word16* wsp, 
			 out Word16* p_wsp_1, 
			 out Word16* p_wsp_2)			 
{
  void main(void)
  {
    p_wsp_1 = wsp;                
    p_wsp_2 = wsp + L_FRAME_BY2;  
  }
};


behavior Ol_Lag_Est_Seq2 (in Word16* p_wsp_1, in Word16* p_wsp_2,
			  out Word16 T_op_1, out Word16 T_op_2)
{
  Pitch_Ol pitch_ol_1(p_wsp_1, T_op_1);
  Pitch_Ol pitch_ol_2(p_wsp_2, T_op_2);
  
  void main(void)
  {
    par
    {
      pitch_ol_1.main(); /* Find open loop pitch lag for last two subframes */
      pitch_ol_2.main(); /* Find open loop pitch lag for last two subframes */
    }
  }
};


behavior Ol_Lag_Est_Seq3 (
			  in Word16 T_op_1,
			  in Word16 T_op_2,
			  out Word16 T0_min_1,
			  out Word16 T0_max_1,
			  out Word16 T0_min_2,
			  out Word16 T0_max_2,
			  in DTXctrl txdtx_ctrl        /* DTX control word */
			  )
{
  Get_Minmax minmax_1(T_op_1, T0_min_1, T0_max_1, txdtx_ctrl);
  Get_Minmax minmax_2(T_op_2, T0_min_2, T0_max_2, txdtx_ctrl);
  
  void main(void)
  {
    par
    {
      /* get range for closed loop pitch search, subframes 1 & 2 */
      minmax_1.main(); 
      /* get range for closed loop pitch search, subframes 2 & 3 */
      minmax_2.main(); 
    }
  }
};


behavior Ol_Lag_Est_Seq4(out Word16 lags[2], in Word16 T_op_1)
{
  void main(void)
  {
    lags[0] = T_op_1;                                              
  }
};


behavior Ol_Lag_Est_Seq5(out Word16 lags[2], in Word16 T_op_2)
{
  void main(void)
  {
    lags[1] = T_op_2;                                              
  }
};


behavior Ol_Lag_Est (
  in  Word16 *wsp,      /* (i)  this is the weighted speech */
  out Word16 T0_min_1,  /* range for closed loop pitch search, subframes 1&2*/
  out Word16 T0_max_1,
  out Word16 T0_min_2,  /* range for closed loop pitch search, subframes 3&4*/
  out Word16 T0_max_2,
  out Flag   ptch,      /* flag to indicate a periodic signal component */
  in  DTXctrl txdtx_ctrl,        /* voice activity flags */
  in  Flag   dtx_mode,
  in  Flag   reset_flag		     
  )
implements Ireset
{
  Word16 lags[2];          /* speech encoder long term predictor lags */

  Word16 T_op_1, T_op_2;
  Word16 *p_wsp_1, *p_wsp_2;

  Ol_Lag_Est_Seq1 seq1(wsp, p_wsp_1, p_wsp_2);
  Ol_Lag_Est_Seq2 seq2(p_wsp_1, p_wsp_2, T_op_1, T_op_2);
  Ol_Lag_Est_Seq3 seq3(T_op_1, T_op_2, T0_min_1, T0_max_1, T0_min_2, T0_max_2, 
		       txdtx_ctrl);
  Ol_Lag_Est_Seq4 seq4(lags, T_op_1);
  Ol_Lag_Est_Seq5 seq5(lags, T_op_2);
  

  Period_Upd periodicity_update(lags, ptch, dtx_mode, reset_flag);



  void reset(void)
    {
      periodicity_update.reset();
    }

  
  void main(void)
  {
    fsm {
      
      seq1:

      seq2:
      
      seq3:
      
      seq4: {                     
	if (dtx_mode == 1) goto seq5;
	goto periodicity_update;
      }
      
      seq5:

      periodicity_update:
    }
    
  }
};
