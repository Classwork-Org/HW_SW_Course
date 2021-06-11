/***************************************************************************
 *     TX DTX handler (called by the speech encoder):
 *       tx_dtx()
 *
 *************************************************************************/

#include "typedef.sh"
#include "cnst.sh"

import "reset";

import "basic_op";




/* Constant DTX_ELAPSED_THRESHOLD is used as threshold for allowing
   SID frame updating without hangover period in case when elapsed
   time measured from previous SID update is below 24 */
#define DTX_ELAPSED_THRESHOLD (24 + DTX_HANGOVER - 1)


/* Pseudo noise generator seed value  */
#define PN_INITIAL_SEED 0x70816958L 



/*************************************************************************
 *
 *   FUNCTION NAME: tx_dtx
 *
 *   PURPOSE: DTX handler of the speech encoder. Determines when to add
 *            the hangover period to the end of the speech burst, and
 *            also determines when to use old SID parameters, and when
 *            to update the SID parameters. This function also initializes
 *            the pseudo noise generator shift register.
 *
 *            Operation of the TX DTX handler is based on the VAD flag
 *            given as input from the speech encoder.
 *
 *   INPUTS:      VAD_flag      Voice activity decision
 *                *txdtx_ctrl   Old encoder DTX control word
 *
 *   OUTPUTS:     *txdtx_ctrl   Updated encoder DTX control word
 *                L_pn_seed_tx  Initialized pseudo noise generator shift
 *                              register (global variable)
 *
 *   RETURN VALUE: none
 *
 *************************************************************************/


behavior TX_Dtx (
		 in  Flag VAD_flag,
		     DTXctrl txdtx_ctrl,
		 out Word32 L_pn_seed_tx,
		 in  Flag reset_flag
		 )
  implements Ireset

{
  Word16 txdtx_hangover;   /* Length of hangover period (VAD=0, SP=1) */
  Word16 txdtx_N_elapsed;  /* Measured time from previous SID frame   */


  void init(void)
    {
      txdtx_hangover = DTX_HANGOVER;
      txdtx_N_elapsed = 0x7fff;

      L_pn_seed_tx = PN_INITIAL_SEED;
    }

  void reset(void)
  {
    init();
  }

  void main(void)
    {
      if (reset_flag) init();
      
      /* N_elapsed (frames since last SID update) is incremented. If SID
	 is updated N_elapsed is cleared later in this function */

      txdtx_N_elapsed = add (txdtx_N_elapsed, 1);

      /* If voice activity was detected, reset hangover counter */

       
      if (sub (VAD_flag, 1) == 0)
	{
	  txdtx_hangover = DTX_HANGOVER;           
	  txdtx_ctrl = TX_SP_FLAG | TX_VAD_FLAG;   
	}
      else
	{
	   
	  if (txdtx_hangover == 0)
	    {
	      /* Hangover period is over, SID should be updated */

	      txdtx_N_elapsed = 0;                 

	      /* Check if this is the first frame after hangover period */
	        
	      if ((txdtx_ctrl & TX_HANGOVER_ACTIVE) != 0)
		{
		  txdtx_ctrl = TX_PREV_HANGOVER_ACTIVE 
		    | TX_SID_UPDATE;
		    L_pn_seed_tx = PN_INITIAL_SEED;  
		}
	      else
		{
		  txdtx_ctrl = TX_SID_UPDATE;     
		}
	    }
	  else
	    {
	      /* Hangover period is not over, update hangover counter */
	      txdtx_hangover = sub (txdtx_hangover, 1);

	      /* Check if elapsed time from last SID update is greater than
		 threshold. If not, set SP=0 (although hangover period is not
		 over) and use old SID parameters for new SID frame.
		 N_elapsed counter must be summed with hangover counter in 
	         order to avoid erroneus SP=1 decision in case when N_elapsed 
	         is grown bigger than threshold and hangover period is still 
	         active */
	       
	      if (sub (add (txdtx_N_elapsed, txdtx_hangover),
		       DTX_ELAPSED_THRESHOLD) < 0)
		{
		  /* old SID frame should be used */
		  txdtx_ctrl = TX_USE_OLD_SID;    
		}
	      else
		{
		    
		  if ((txdtx_ctrl & TX_HANGOVER_ACTIVE) != 0)
		    {
		      txdtx_ctrl = TX_PREV_HANGOVER_ACTIVE
                        | TX_HANGOVER_ACTIVE
                        | TX_SP_FLAG;      
		    }
		  else
		    {
		      txdtx_ctrl = TX_HANGOVER_ACTIVE
                        | TX_SP_FLAG;             
		    }
		}
	    }
	}
    }
};

