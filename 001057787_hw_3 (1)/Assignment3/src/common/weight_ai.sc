/*************************************************************************
 *
 *  FUNCTION:  Weight_Ai
 *
 *  PURPOSE: Spectral expansion of LP coefficients.  (order==10)
 *
 *  DESCRIPTION:
 *      a_exp[i] = a[i] * fac[i-1]    ,i=1,10
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";


behavior Weight_Ai (
    in  Word16 a[MP1],      /* (i)     : a[MP1]  LPC coefficients            */
    in  Word16 fac[M],      /* (i)     : Spectral expansion factors.         */
    out Word16 a_exp[MP1]   /* (o)     : Spectral expanded LPC coefficients  */
)
{
  void main(void)
    {
      Int i;
      
      a_exp[0] = a[0];                                     
      for (i = 1; i <= M; i++)
	{
	  a_exp[i] = round (L_mult (a[i], fac[i - 1]));    
	}
      
    }
};
