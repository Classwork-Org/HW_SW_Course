/*************************************************************************
 *
 *  FUNCTION:  Residu
 *
 *  PURPOSE:  Computes the LP residual.
 *
 *  DESCRIPTION:
 *     The LP residual is computed by filtering the input speech through
 *     the LP inverse filter A(z).
 *
 *************************************************************************/
//
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";


behavior Residu (
    in  Word16 a[MP1],      /* (i)     : prediction coefficients */
    in  Word16 *x,          /* (i)     : speech signal           */
    out Word16 y[L_SUBFR]   /* (o)     : residual signal         */
)
{
  void main(void)
    {
      Int i, j;
      Word32 s;
      
      for (i = 0; i < L_SUBFR; i++)
	{
	  s = L_mult (x[i], a[0]);
	  for (j = 1; j <= M; j++)
	    {
	      s = L_mac (s, a[j], x[i - j]);
	    }
	  s = L_shl (s, 3);
	  y[i] = round (s);        
	}
    }
};
