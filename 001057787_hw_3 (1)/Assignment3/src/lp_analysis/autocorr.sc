/*************************************************************************
 *
 *  FUNCTION:  autocorr
 *
 *  PURPOSE:   Compute autocorrelations of signal with windowing
 *
 *  DESCRIPTION:
 *       - Windowing of input speech:   s'[n] = s[n] * w[n]
 *       - Autocorrelations of input speech:
 *             r[k] = sum_{i=k}^{239} s'[i]*s'[i-k]    k=0,...,10
 *         The autocorrelations are expressed in normalized double precision
 *         format.
 *
 *************************************************************************/

#include "typedef.sh"
#include "cnst.sh"
#include "min_max.sh"

import "basic_op";


behavior Autocorr (
   in  Word16 *x,            /* (i): Input signal                    */
   in  Word16 wind[L_WINDOW],/* (i): window for LPC analysis         */
   out Word16 r_h[MP1],      /* (o): Autocorrelations  (msb)         */
   out Word16 r_l[MP1],      /* (o): Autocorrelations  (lsb)         */
   out Word16 norm           /* (o): scaling factor for the autocorrelations */
   )
{
  void main(void)
    {
      Int i, j;
      Word16 n;
      Word16 y[L_WINDOW];
      Word32 sum;
      Flag overfl;
      Word16 overfl_shft;

      /* Windowing of signal */

      for (i = 0; i < L_WINDOW; i++)
	{
	  y[i] = mult_r (x[i], wind[i]);  
	}

      /* Compute r[0] and test for overflow */

      overfl_shft = 0;                    

      do
	{
	  overfl = 0;                     
	  sum = 0L;                      

	  for (i = 0; i < L_WINDOW; i++)
	    {
	      sum = L_mac (sum, y[i], y[i]);
	    }

	  /* If overflow divide y[] by 4 */

	   
	  if (L_sub (sum, MAX_32) == 0L)
	    {
	      overfl_shft = add (overfl_shft, 4);
	      overfl = 1;                 /* Set the overflow flag */

	      for (i = 0; i < L_WINDOW; i++)
		{
		  y[i] = shr (y[i], 2);   
		}
	    }
	   
	}
      while (overfl != 0);

      sum = L_add (sum, 1L);             /* Avoid the case of all zeros */

      /* Normalization of r[0] */

      n = norm_l (sum);
      sum = L_shl (sum, n);
      L_Extract (sum, &r_h[0], &r_l[0]); /* Put in DPF format (see oper_32b) */

      /* r[1] to r[M] */

      for (i = 1; i <= M; i++)
	{
	  sum = 0;                        

	  for (j = 0; j < L_WINDOW - i; j++)
	    {
	      sum = L_mac (sum, y[j], y[j + i]);
	    }

	  sum = L_shl (sum, n);
	  L_Extract (sum, &r_h[i], &r_l[i]);
	}

      norm = sub (n, overfl_shft);
    }
};
