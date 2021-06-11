/*************************************************************************
 *
 *  FUNCTION:  Syn_filt:
 *
 *  PURPOSE:  Perform synthesis filtering through 1/A(z).
 *
 *************************************************************************/
//
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "cnst.sh"
#include "typedef.sh"


import "basic_op";



behavior Syn_Filt (
    in  Word16 a[MP1],     /* (i)  : a[m+1] prediction coefficients   (m=10) */
    in  Word16 x[L_SUBFR], /* (i)  : input signal                            */
    out Word16 y[L_SUBFR], /* (o)  : output signal                           */
        Word16 mem[M],     /* (i/o): memory associated with this filtering.  */
    in  bool update        /* (i)  : 0=no update, 1=update of memory.        */
)
{
  void main(void)
    {
      Int i, j;
      Word32 s;
      Word16 tmp[80];   /* This is usually done by memory allocation (lg+m) */
      Word16 *yy;
      
      /* Copy mem[] to yy[] */
      
      yy = tmp;                            
      
      for (i = 0; i < M; i++)
	{
	  *yy++ = mem[i];                  
	} 
      
      /* Do the filtering. */
      
      for (i = 0; i < L_SUBFR; i++)
	{
	  s = L_mult (x[i], a[0]);
	  for (j = 1; j <= M; j++)
	    {
	      s = L_msu (s, a[j], yy[-j]);
	    }
	  s = L_shl (s, 3);
	  *yy++ = round (s);               
	}
      
      for (i = 0; i < L_SUBFR; i++)
	{
	  y[i] = tmp[i + M];               
	}
      
      /* Update of memory if update==1 */
      
       
      if (update != 0)
	{
	  for (i = 0; i < M; i++)
	    {
	      mem[i] = tmp[L_SUBFR + i];      
	    }
	}
    }
};
