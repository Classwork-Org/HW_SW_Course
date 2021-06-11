#include "typedef.sh"
#include "cnst.sh"

import "array_op";


behavior Shift_Signals (
  Word16 old_speech[L_TOTAL],                     /* Speech vector */
  Word16 old_wsp[L_FRAME + PIT_MAX],              /* Weighted speech vector */
  Word16 old_exc[L_FRAME + PIT_MAX + L_INTERPOL], /* Excitation vector */
  in  DTXctrl txdtx_ctrl_cur,
  out DTXctrl txdtx_ctrl
  )
{
  void main(void)
    {
      /*--------------------------------------------------*
       * Update signal for next frame.                    *
       * -> shift to the left by L_FRAME:                 *
       *     speech[], wsp[] and  exc[]                   *
       *--------------------------------------------------*/
      
      Copy (&old_speech[L_FRAME], &old_speech[0], L_TOTAL - L_FRAME);
      
      
      Copy (&old_wsp[L_FRAME], &old_wsp[0], PIT_MAX);
      
      
      Copy (&old_exc[L_FRAME], &old_exc[0], PIT_MAX + L_INTERPOL);

      txdtx_ctrl = txdtx_ctrl_cur;
    }
};
