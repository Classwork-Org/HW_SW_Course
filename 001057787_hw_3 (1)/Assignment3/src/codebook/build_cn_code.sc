/*************************************************************************
 *
 *   FUNCTION NAME: build_CN_code
 *
 *   PURPOSE: Compute the comfort noise fixed codebook excitation. The
 *            gains of the pulses are always +/-1.
 *
 *   INPUTS:      *seed         Old CN generator shift register state
 *
 *   OUTPUTS:     cod[0..39]    Generated comfort noise fixed codebook vector
 *                *seed         Updated CN generator shift register state
 *
 *   RETURN VALUE: none
 *
 *************************************************************************/

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";



#define NB_PULSE 10       /* Number of pulses in fixed codebook excitation */



behavior Build_CN_Code (
			out Word16 cod[L_SUBFR],
			inout Word32 seed
			)
{


  /*************************************************************************
 *
 *   FUNCTION NAME: pseudonoise
 *
 *   PURPOSE: Generate a random integer value to use in comfort noise
 *            generation. The algorithm uses polynomial x^31 + x^3 + 1
 *            (length of PN sequence is 2^31 - 1).
 *
 *   INPUTS:      seed    Old CN generator shift register state
 *
 *
 *   OUTPUTS:     seed    Updated CN generator shift register state
 *
 *   RETURN VALUE: Generated random integer value
 *
 *************************************************************************/

  Word16 pseudonoise (
		      Word16 no_bits
		      )
    {
      Int i;
      Word16 noise_bits, Sn;

      noise_bits = 0;                                     
      for (i = 0; i < no_bits; i++)
	{
	  /* State n == 31 */
	    
	  if ((seed & 0x00000001L) != 0)
	    {
	      Sn = 1;                                     
	    }
	  else
	    {
	      Sn = 0;                                     
	    }

	  /* State n == 3 */
	    
	  if ((seed & 0x10000000L) != 0)
	    {
	      Sn = Sn ^ 1;                                
	    }
	  else
	    {
	      Sn = Sn ^ 0;                                
	    }

	  noise_bits = shl (noise_bits, 1);
	  noise_bits = noise_bits | (extract_l (seed) & 1);
	    

	  seed = L_shr (seed, 1);             
	    
	  if (Sn & 1)
	    {
	      seed = seed | 0x40000000L;       
	    }
	}

      return noise_bits;
    }



  void main(void)
    {
      Word16 i, j;
      Int k;

      for (k = 0; k < L_SUBFR; k++)
	{
	  cod[k] = 0;                                     
	}

      for (k = 0; k < NB_PULSE; k++)
	{
	  i = pseudonoise (2);      /* generate pulse position */
	  i = shr (extract_l (L_mult (i, 10)), 1);
	  i = add (i, k);

	  j = pseudonoise (1);      /* generate sign           */

	   
	  if (j > 0)
	    {
	      cod[i] = 4096;                              
	    }
	  else
	    {
	      cod[i] = -4096;                             
	    }
	}

    }
};
