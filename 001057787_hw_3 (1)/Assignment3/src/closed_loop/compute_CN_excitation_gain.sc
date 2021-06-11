/*************************************************************************
 *
 *   FUNCTION NAME: compute_CN_excitation_gain
 *
 *   PURPOSE: Compute the unquantized fixed codebook gain. Computation is
 *            based on the energy of the Linear Prediction residual signal.
 *
 *   INPUTS:      res2[0..39]   Linear Prediction residual signal
 *
 *   OUTPUTS:     none
 *
 *   RETURN VALUE: Unquantized fixed codebook gain
 *
 *************************************************************************/

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";


#include "min_max.sh"


behavior Compute_CN_Excitation_Gain(
				    in Word16 res2[L_SUBFR],
				    out Word16 c_gain
				    )
{

  void main(void)
    {
      Int i;
      Word16 gain;
      Word16 norm, norm1;
      Flag overfl;
      Word32 L_temp;

      /* Compute the energy of the LP residual signal */

      norm = 0;                                           
      do
	{
	  overfl = 0;                                    

	  L_temp = 0L;                                    
	  for (i = 0; i < L_SUBFR; i++)
	    {
	      gain = shr (res2[i], norm);
	      L_temp = L_mac (L_temp, gain, gain);
	    }

	   
	  if (L_sub (L_temp, MAX_32) == 0)
	    {
	      norm = add (norm, 1);
	      overfl = 1;                  /* Set the overflow flag */
	    }
	   
	}
      while (overfl != 0);

      L_temp = L_add (L_temp, 1L);          /* Avoid the case of all zeros */

      /* Take the square root of the obtained energy value (sqroot is a 2nd
	 order Taylor series approximation) */

      norm1 = norm_l (L_temp);
      gain = extract_h (L_shl (L_temp, norm1));
      L_temp = L_mult (gain, gain);
      L_temp = L_sub (805306368L, L_shr (L_temp, 3));
      L_temp = L_add (L_temp, L_mult (24576, gain));

      gain = extract_h (L_temp);
        
      if ((norm1 & 0x0001) != 0)
	{
	  gain = mult_r (gain, 23170);
	  norm1 = sub (norm1, 1);
	}
      /* Divide the result of sqroot operation by sqroot(10) */

      gain = mult_r (gain, 10362);

      /* Re-scale to get the final value */

      norm1 = shr (norm1, 1);
      norm1 = sub (norm1, norm);

       
      if (norm1 >= 0)
	{
	  c_gain = shr (gain, norm1);
	}
      else
	{
	  c_gain = shl (gain, abs_s (norm1));
	}

    }
};
