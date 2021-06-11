#include "typedef.sh"
#include "cnst.sh"

import "reset";
import "copy";

import "vad_comp";
import "tx_dtx";
import "nodtx_setflags";




behavior Vad_Lp (
 in  Word16 r_l[MP1], 
 in  Word16 r_h[MP1],
 in  Word16 scal_fac,
 in  Word16 rc[4],           /* rc[4]   First 4 reflection coefficients */
 in  Flag ptch,              /* flag to indicate a periodic signal component */
     DTXctrl txdtx_ctrl,     /* DTX control word  */
 in  Flag dtx_mode,
 out Word32 L_pn_seed_tx,
 in  Flag reset_flag		 
)
implements Ireset
{
  Flag VAD_flag;

  Nop nop;
  Nodtx_Setflags nodtx_setflags(VAD_flag, txdtx_ctrl);
  VAD_Computation vad_computation(r_h, r_l, scal_fac, rc, ptch, VAD_flag, 
				  reset_flag);
  TX_Dtx tx_dtx(VAD_flag, txdtx_ctrl, L_pn_seed_tx, reset_flag);
  

void reset(void)
  {
    vad_computation.reset();
    tx_dtx.reset();
  }


  void main(void)
    {

      fsm 
	{
	nop: {
	  if (dtx_mode == 1)    /* DTX enabled, make voice activity decision */
	    goto vad_computation;
	  /* DTX disabled, active speech in every frame */
	  /* Goto nodtx_setflags */ 
	}
	
	nodtx_setflags: {
	  break;
	}
	
	vad_computation: {
	  /* goto tx_dtx */
	}
	
	tx_dtx: {
	  break;
	}
	
	} /* msf */
      
    }
};
