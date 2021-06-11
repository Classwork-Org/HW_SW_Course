#include "cnst.sh"
#include "typedef.sh"


import "reset";



/*************************************************************************
 *
 *   FUNCTION NAME: cn_encoding
 *
 *   PURPOSE:  Encoding of the comfort noise parameters into a SID frame.
 *             Use old SID parameters if necessary. Set the parameter
 *             indices not used by comfort noise parameters to zero.
 *
 *   INPUTS:      params[0..56]  Comfort noise parameter frame from the
 *                               speech encoder
 *                txdtx_ctrl     TX DTX handler control word
 *
 *   OUTPUTS:     params[0..56]  Comfort noise encoded parameter frame
 *
 *   RETURN VALUE: none
 *
 *************************************************************************/



behavior Cn_Encoder (
		      in  Word16 paramsi[PRM_SIZE],
		      out Word16 paramso[PRM_SIZE],
		      in  DTXctrl txdtx_ctrl,
		      in  Flag reset_flag
		      )
  implements Ireset

{

  Word16 old_CN_mem_tx[6]; /* The most recent CN parameters are stored*/


  void init(void)
    {
      Int i;

      for (i = 0; i < 6; i++)
	{
	  old_CN_mem_tx[i] = 0;
	}
    }

  void reset(void)
  {
    init();
  }

  void main(void)
    {
      Int i;

      if (reset_flag) init();
      
      if ((txdtx_ctrl & TX_SP_FLAG) == 0)
      {
	if ((txdtx_ctrl & TX_SID_UPDATE) != 0)
	{
	  /* Store new CN parameters in memory to be used later as old
	   CN parameters */
	  
	  /* LPC parameter indices */
	  for (i = 0; i < 5; i++)
	  {
	    old_CN_mem_tx[i] = paramsi[i];
	  }
	  /* Codebook index computed in last subframe */
	  old_CN_mem_tx[5] = paramsi[56];   
	}
	if ((txdtx_ctrl & TX_USE_OLD_SID) != 0)
	{
	  /* Use old CN parameters previously stored in memory */
	  for (i = 0; i < 5; i++)
	  {
	    paramso[i] = old_CN_mem_tx[i]; 
	  }
	  paramso[17] = old_CN_mem_tx[5];      
	  paramso[30] = old_CN_mem_tx[5];        
	  paramso[43] = old_CN_mem_tx[5];      
	  paramso[56] = old_CN_mem_tx[5];       
	}
	/* Set all the rest of the parameters to zero (SID codeword will
						       be written later) */
	for (i = 0; i < 12; i++)
	{
	  paramso[i + 5] = 0;                      
	  paramso[i + 18] = 0;                      
	  paramso[i + 31] = 0;                      
	  paramso[i + 44] = 0;                        
	}
      }
      else {
	  paramso = paramsi;
      }
    }
};


