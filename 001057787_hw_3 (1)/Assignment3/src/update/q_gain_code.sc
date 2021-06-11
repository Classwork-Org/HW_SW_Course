/*--------------------------------------------------------------------------*
 * Function q_gain_code()                                                   *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~                                  *
 * Scalar quantization of the pitch gain and the innovative codebook gain.  *
 *                                                                          *
 * MA prediction is performed on the innovation energy                      *
 * (in dB/(20*log10(2))) with mean removed.                                 *
 *-------------------------------------------------------------------------*/
//
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "basic_op";
import "basic_func";




#define NB_QUA_CODE 32

/* average innovation energy.                             */
/* MEAN_ENER  = 36.0/constant, constant = 20*Log10(2)     */
#define MEAN_ENER  783741L




behavior Q_Gain_Code (    
     in  Int i_subfr,
     in  Word16 code[L_SUBFR],    /* (i)  : fixed codebook excitation       */
     in  Word16 gain,             /* (i/o): quantized fixed codebook gain   */
     out Word16 qgain,
     in  DTXctrl txdtx_ctrl,
     in  Word16 CN_excitation_gain,
     out Word16 index,             /* Return quantization index              */
     in  Flag   reset_flag		      
     )
implements Ireset
{
  Word16 past_qua_en[4]; /* past quantized energies, initialized 
			    to -14.0/constant, constant = 20*Log10(2)   */
  Word16 pred[4];        /* MA prediction coeff */

  Word16 buf_p_tx;
  /* Comfort noise gain averaging buffer   */
  Word16 gain_code_old_tx[4 * DTX_HANGOVER]; 



  void init(void)
    {
      Int i;

      for (i = 0; i < 4; i++)
	{
	  past_qua_en[i] = -2381; /* past quantized energies */
	}      
      pred[0] = 44;               /* MA prediction coeff */
      pred[1] = 37;               /* MA prediction coeff */
      pred[2] = 22;               /* MA prediction coeff */
      pred[3] = 12;               /* MA prediction coeff */      

      for (i = 0; i < 4 * DTX_HANGOVER; i++)
	{
	  gain_code_old_tx[i] = 0;
	}

      buf_p_tx = 0;                 /* for Update_gain_code_history_tx */

    }

  
  void reset(void)
  {
    init();
  }


  /*************************************************************************
   *
   *   FUNCTION NAME: update_gain_code_history_tx
   *
   *   PURPOSE: Update the fixed codebook gain parameter history of the
   *            encoder. The fixed codebook gain parameters kept in the buffer
   *            are used later for computing the reference fixed codebook
   *            gain parameter value and the averaged fixed codebook gain
   *            parameter value.
   *
   *   INPUTS:      new_gain_code   New fixed codebook gain value
   *
   *                gain_code_old_tx[0..4*DTX_HANGOVER-1]
   *                                Old fixed codebook gain history of encoder
   *
   *   OUTPUTS:     gain_code_old_tx[0..4*DTX_HANGOVER-1]
   *                            Updated fixed codebook gain history of encoder
   *
   *   RETURN VALUE: none
   *
   *************************************************************************/

  void Update_gain_code_history_tx (
				    Word16 new_gain_code
				    )
    {

      /* Circular buffer */
      gain_code_old_tx[buf_p_tx] = new_gain_code;         

       
      if (sub (buf_p_tx, (4 * DTX_HANGOVER - 1)) == 0)
	{
	  buf_p_tx = 0;                                   
	}
      else
	{
	  buf_p_tx = add (buf_p_tx, 1);
	}

      return;
    }


  /*************************************************************************
   *
   *   FUNCTION NAME: aver_gain_code_history
   *
   *   PURPOSE: Compute the averaged fixed codebook gain parameter value.
   *            Computation is performed by averaging the fixed codebook
   *            gain parameter values which exist in the fixed codebook
   *            gain parameter history, together with the fixed codebook
   *            gain parameter value of the current subframe.
   *
   *   INPUTS:      CN_excitation_gain
   *                              Unquantized fixed codebook gain value
   *                              of the current subframe
   *                gain_code_old[0..4*DTX_HANGOVER-1]
   *                              fixed codebook gain parameter history
   *
   *   OUTPUTS:     none
   *
   *   RETURN VALUE: Averaged fixed codebook gain value
   *
   *************************************************************************/

  Word16 Aver_gain_code_history (
				 Word16 CN_exc_gain,
				 Word16 gain_code_old[4 * DTX_HANGOVER]
				 )
    {
      Int i;
      Word32 L_ret;

      L_ret = L_mult (0x470, CN_exc_gain);

      for (i = 0; i < (4 * DTX_HANGOVER); i++)
	{
	  L_ret = L_mac (L_ret, 0x470, gain_code_old[i]);
	}
      return extract_h (L_ret);
    }



  /*************************************************************************
   *
   *   FUNCTION NAME: update_gcode0_CN
   *
   *   PURPOSE: Update the reference fixed codebook gain parameter value.
   *            The reference value is computed by averaging the quantized
   *            fixed codebook gain parameter values which exist in the
   *            fixed codebook gain parameter history.
   *
   *   INPUTS:      gain_code_old[0..4*DTX_HANGOVER-1]
   *                              fixed codebook gain parameter history
   *
   *   OUTPUTS:     none
   *
   *   RETURN VALUE: Computed reference fixed codebook gain
   *
   *************************************************************************/

  Word16 update_gcode0_CN (
			   Word16 gain_code_old[4 * DTX_HANGOVER]
			   )
    {
      Int i, j;
      Word32 L_temp, L_ret;

      L_ret = 0L;                                         
      for (i = 0; i < DTX_HANGOVER; i++)
	{
	  L_temp = L_mult (0x1fff, gain_code_old[4 * i]);
	  for (j = 1; j < 4; j++)
	    {
	      L_temp = L_mac (L_temp, 0x1fff, gain_code_old[4 * i + j]);
	    }
	  L_ret = L_mac (L_ret, INV_DTX_HANGOVER, extract_h (L_temp));
	}

      return extract_h (L_ret);
    }




  void main(void)
    {
      Int i;
      Word16 gcode0, err, err_min, exp, frac;
      Word32 ener, ener_code;
      Word16 aver_gain;
      static Word16 gcode0_CN;

      
      /*---------------------------------------------------------------------*
       *  Scalar quantization table of the codebook gain.                    *
       *---------------------------------------------------------------------*/
      const Word16 qua_gain_code[NB_QUA_CODE] =
      {
	159, 206, 268, 349, 419, 482, 554, 637,
	733, 842, 969, 1114, 1281, 1473, 1694, 1948,
	2241, 2577, 2963, 3408, 3919, 4507, 5183, 5960,
	6855, 7883, 9065, 10425, 12510, 16263, 21142, 27485
      };

      if (reset_flag) init();
        
      if ((txdtx_ctrl & TX_SP_FLAG) != 0)
	{

	  /*-----------------------------------------------------------------*
	   *  energy of code:                                                *
	   *  ~~~~~~~~~~~~~~~                                                *
	   *  ener_code(Q17) = 10 * Log10(energy/L_SUBFR) / constant         *
	   *                 = 1/2 * Log2(energy/L_SUBFR)                    *
	   *                                           constant = 20*Log10(2)*
	   *-----------------------------------------------------------------*/

	  /* ener_code = log10(ener_code/L_SUBFR) / (20*log10(2))       */
	  ener_code = 0;                           
	  for (i = 0; i < L_SUBFR; i++)
	    {
	      ener_code = L_mac (ener_code, code[i], code[i]);
	    }
	  /* ener_code = ener_code / L_SUBFR */
	  ener_code = L_mult (round (ener_code), 26214);

	  /* ener_code = 1/2 * Log2(ener_code) */
	  Log2 (ener_code, &exp, &frac);
	  ener_code = L_Comp (sub (exp, 30), frac);

	  /* predicted energy */

	  ener = MEAN_ENER;                        
	  for (i = 0; i < 4; i++)
	    {
	      ener = L_mac (ener, past_qua_en[i], pred[i]);
	    }

	  /*-----------------------------------------------------------------*
	   *  predicted codebook gain                                        *
	   *  ~~~~~~~~~~~~~~~~~~~~~~~                                        *
	   *  gcode0(Qx) = Pow10( (ener*constant - ener_code*constant) / 20 )*
	   *             = Pow2(ener-ener_code)                              *
	   *                                           constant = 20*Log10(2)*
	   *-----------------------------------------------------------------*/

	  ener = L_shr (L_sub (ener, ener_code), 1);
	  L_Extract (ener, &exp, &frac);

	  gcode0 = extract_l (Pow2 (exp, frac));  /* predicted gain */

	  gcode0 = shl (gcode0, 4);

	  /*-----------------------------------------------------------------*
	   *                   Search for best quantizer                     *
	   *-----------------------------------------------------------------*/

	  err_min = abs_s (sub (gain, mult (gcode0, qua_gain_code[0])));
	  index = 0;               

	  for (i = 1; i < NB_QUA_CODE; i++)
	    {
	      err = abs_s (sub (gain, mult (gcode0, qua_gain_code[i])));

	       
	      if (sub (err, err_min) < 0)
		{
		  err_min = err;                   
		  index = i;                       
		}
	    }

	  qgain = gcode0 = mult (gcode0, qua_gain_code[index]);
	   

	  /*------------------------------------------------------------------*
	   *  update table of past quantized energies                         *
	   *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~                         *
	   *  past_qua_en(Q12) = 20 * Log10(qua_gain_code) / constant         *
	   *                   = Log2(qua_gain_code)                          *
	   *                                           constant = 20*Log10(2) *
	   *-----------------------------------------------------------------*/

	  for (i = 3; i > 0; i--)
	    {
	      past_qua_en[i] = past_qua_en[i - 1]; 
	    }
	  Log2 (L_deposit_l (qua_gain_code[index]), &exp, &frac);

	  past_qua_en[0] = shr (frac, 5);          
	  past_qua_en[0] = add (past_qua_en[0], shl (sub (exp, 11), 10));
	   

	  Update_gain_code_history_tx (gcode0);
	}
      else
	{
	     
	  if ((txdtx_ctrl & TX_PREV_HANGOVER_ACTIVE) != 0 && (i_subfr == 0))
	    {
	      gcode0_CN = update_gcode0_CN (gain_code_old_tx);
	      gcode0_CN = shl (gcode0_CN, 4);
	    }
	  qgain = gcode0 = CN_excitation_gain;

	     
	  if ((txdtx_ctrl & TX_SID_UPDATE) != 0)
	    {
	      aver_gain = Aver_gain_code_history (CN_excitation_gain,
						  gain_code_old_tx);

	      /*-------------------------------------------------------------*
	       *                   Search for best quantizer                 *
	       *-------------------------------------------------------------*/

	      err_min = abs_s (sub (aver_gain, 
				    mult (gcode0_CN, qua_gain_code[0])));
	      index = 0;                           

	      for (i = 1; i < NB_QUA_CODE; i++)
		{
		  err = abs_s (sub (aver_gain, 
				    mult (gcode0_CN, qua_gain_code[i])));

		   
		  if (sub (err, err_min) < 0)
		    {
		      err_min = err;               
		      index = i;                   
		    }
		}
	    }
	  Update_gain_code_history_tx (gcode0);

	  /*-----------------------------------------------------------------*
	   *  reset table of past quantized energies                         *
	   *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~                         *
	   *-----------------------------------------------------------------*/

	  for (i = 0; i < 4; i++)
	    {
	      past_qua_en[i] = -2381;              
	    }
	}
    }
};
