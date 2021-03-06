/***********************************************************************
 *
 *  FUNCTION:  Az_lsp
 *
 *  PURPOSE:   Compute the LSPs from  the LP coefficients  (order=10)
 *
 *  DESCRIPTION:
 *    - The sum and difference filters are computed and divided by
 *      1+z^{-1}   and   1-z^{-1}, respectively.
 *
 *         f1[i] = a[i] + a[11-i] - f1[i-1] ;   i=1,...,5
 *         f2[i] = a[i] - a[11-i] + f2[i-1] ;   i=1,...,5
 *
 *    - The roots of F1(z) and F2(z) are found using Chebyshev polynomial
 *      evaluation. The polynomials are evaluated at 60 points regularly
 *      spaced in the frequency domain. The sign change interval is
 *      subdivided 4 times to better track the root.
 *      The LSPs are found in the cosine domain [1,-1].
 *
 *    - If less than 10 roots are found, the LSPs from the past frame are
 *      used.
 *
 ***********************************************************************/

#include "typedef.sh"
#include "cnst.sh"
#include "min_max.sh"

import "basic_op";



/* M = LPC order, NC = M/2 */
#define NC   M/2


#define grid_points 60



behavior Az_Lsp (
  in  Word16 a[MP1],       /* (i) [MP1]: predictor coefficients           */
  out Word16 lsp[M],       /* (o): line spectral pairs                    */
  in  Word16 old_lsp[M]    /* (i): old lsp[] (in case not found 10 roots) */
  )
{

  /************************************************************************
   *
   *  FUNCTION:  Chebps
   *
   *  PURPOSE:   Evaluates the Chebyshev polynomial series
   *
   *  DESCRIPTION:
   *  - The polynomial order is   n = m/2 = 5
   *  - The polynomial F(z) (F1(z) or F2(z)) is given by
   *     F(w) = 2 exp(-j5w) C(x)
   *    where
   *      C(x) = T_n(x) + f(1)T_n-1(x) + ... +f(n-1)T_1(x) + f(n)/2
   *    and T_m(x) = cos(mw) is the mth order Chebyshev polynomial ( x=cos(w) )
   *  - The function returns the value of C(x) for the input x.
   *
   ***********************************************************************/

  Word16 Chebps (Word16 x, Word16 f[], Word16 n)
    {
      Int i;	
      Word16 cheb;
      Word16 b0_h, b0_l, b1_h, b1_l, b2_h, b2_l;
      Word32 t0;

      b2_h = 256;                     /* b2 = 1.0 */
      b2_l = 0;                       

      t0 = L_mult (x, 512);          /* 2*x                 */
      t0 = L_mac (t0, f[1], 8192);   /* + f[1]              */
      L_Extract (t0, &b1_h, &b1_l);  /* b1 = 2*x + f[1]     */

      for (i = 2; i < n; i++)
	{
	  t0 = Mpy_32_16 (b1_h, b1_l, x);         /* t0 = 2.0*x*b1        */
	  t0 = L_shl (t0, 1);
	  t0 = L_mac (t0, b2_h, (Word16) 0x8000); /* t0 = 2.0*x*b1 - b2   */
	  t0 = L_msu (t0, b2_l, 1);
	  t0 = L_mac (t0, f[i], 8192);        /* t0 = 2.0*x*b1 - b2 + f[i] */

	  L_Extract (t0, &b0_h, &b0_l);       /* b0 = 2.0*x*b1 - b2 + f[i]*/

	  b2_l = b1_l;                  /* b2 = b1; */
	  b2_h = b1_h;                
	  b1_l = b0_l;                  /* b1 = b0; */
	  b1_h = b0_h;                
	}

      t0 = Mpy_32_16 (b1_h, b1_l, x);             /* t0 = x*b1; */
      t0 = L_mac (t0, b2_h, (Word16) 0x8000);     /* t0 = x*b1 - b2   */
      t0 = L_msu (t0, b2_l, 1);
      t0 = L_mac (t0, f[i], 4096);                /* t0 = x*b1 - b2 + f[i]/2 */

      t0 = L_shl (t0, 6);

      cheb = extract_h (t0);

      return (cheb);
    }


  void main(void)
    {
      Int i, j;
      Int nf, ip;
      Word16 xlow, ylow, xhigh, yhigh, xmid, ymid, xint;
      Word16 x, y, sign, exp;
      Word16 *coef;
      Word16 f1[M / 2 + 1], f2[M / 2 + 1];
      Word32 t0;

      /*-------------------------------------------------------------*
       *  Table for az_lsf()                                         *
       *                                                             *
       * grid[0] = 1.0;                                              *
       * grid[grid_points+1] = -1.0;                                 *
       * for (i = 1; i < grid_points; i++)                           *
       *   grid[i] = cos((6.283185307*i)/(2.0*grid_points));         *
       *                                                             *
       *-------------------------------------------------------------*/
      
      const Word16 grid[grid_points + 1] =
      {
	32760, 32723, 32588, 32364, 32051, 31651,
	31164, 30591, 29935, 29196, 28377, 27481,
	26509, 25465, 24351, 23170, 21926, 20621,
	19260, 17846, 16384, 14876, 13327, 11743,
	10125, 8480, 6812, 5126, 3425, 1714,
	0, -1714, -3425, -5126, -6812, -8480,
	-10125, -11743, -13327, -14876, -16384, -17846,
	-19260, -20621, -21926, -23170, -24351, -25465,
	-26509, -27481, -28377, -29196, -29935, -30591,
	-31164, -31651, -32051, -32364, -32588, -32723,
	-32760
      };


      /*-------------------------------------------------------------*
       *  find the sum and diff. pol. F1(z) and F2(z)                *
       *    F1(z) <--- F1(z)/(1+z**-1) & F2(z) <--- F2(z)/(1-z**-1)  *
       *                                                             *
       * f1[0] = 1.0;                                                *
       * f2[0] = 1.0;                                                *
       *                                                             *
       * for (i = 0; i< NC; i++)                                     *
       * {                                                           *
       *   f1[i+1] = a[i+1] + a[M-i] - f1[i] ;                       *
       *   f2[i+1] = a[i+1] - a[M-i] + f2[i] ;                       *
       * }                                                           *
       *-------------------------------------------------------------*/

      f1[0] = 1024;                   /* f1[0] = 1.0 */
      f2[0] = 1024;                   /* f2[0] = 1.0 */

      for (i = 0; i < NC; i++)
	{
	  t0 = L_mult (a[i + 1], 8192);   /* x = (a[i+1] + a[M-i]) >> 2  */
	  t0 = L_mac (t0, a[M - i], 8192);
	  x = extract_h (t0);
	  /* f1[i+1] = a[i+1] + a[M-i] - f1[i] */
	  f1[i + 1] = sub (x, f1[i]); 

	  t0 = L_mult (a[i + 1], 8192);   /* x = (a[i+1] - a[M-i]) >> 2 */
	  t0 = L_msu (t0, a[M - i], 8192);
	  x = extract_h (t0);
	  /* f2[i+1] = a[i+1] - a[M-i] + f2[i] */
	  f2[i + 1] = add (x, f2[i]); 
	}

      /*-------------------------------------------------------------*
       * find the LSPs using the Chebychev pol. evaluation           *
       *-------------------------------------------------------------*/

      nf = 0;                         /* number of found frequencies */
      ip = 0;                         /* indicator for f1 or f2      */

      coef = f1;                      

      xlow = grid[0];                 
      ylow = Chebps (xlow, coef, NC); 

      j = 0;
        
      /* while ( (nf < M) && (j < grid_points) ) */
      while ((sub (nf, M) < 0) && (sub (j, grid_points) < 0))
	{
	  j++;
	  xhigh = xlow;               
	  yhigh = ylow;               
	  xlow = grid[j];             
	  ylow = Chebps (xlow, coef, NC);
	   

	   
	  if (L_mult (ylow, yhigh) <= (Word32) 0L)
	    {

	      /* divide 4 times the interval */

	      for (i = 0; i < 4; i++)
		{
		  /* xmid = (xlow + xhigh)/2 */
		  xmid = add (shr (xlow, 1), shr (xhigh, 1));
		  ymid = Chebps (xmid, coef, NC);
		   

		   
		  if (L_mult (ylow, ymid) <= (Word32) 0L)
		    {
		      yhigh = ymid;   
		      xhigh = xmid;   
		    }
		  else
		    {
		      ylow = ymid;    
		      xlow = xmid;    
		    }
		}

	      /*-------------------------------------------------------------*
	       * Linear interpolation                                        *
	       *    xint = xlow - ylow*(xhigh-xlow)/(yhigh-ylow);            *
	       *-------------------------------------------------------------*/

	      x = sub (xhigh, xlow);
	      y = sub (yhigh, ylow);

	       
	      if (y == 0)
		{
		  xint = xlow;        
		}
	      else
		{
		  sign = y;           
		  y = abs_s (y);
		  exp = norm_s (y);
		  y = shl (y, exp);
		  y = div_s ((Word16) 16383, y);
		  t0 = L_mult (x, y);
		  t0 = L_shr (t0, sub (20, exp));
		  y = extract_l (t0);     /* y= (xhigh-xlow)/(yhigh-ylow) */

		   
		  if (sign < 0)
                    y = negate (y);

		  t0 = L_mult (ylow, y);
		  t0 = L_shr (t0, 11);
		  xint = sub (xlow, extract_l (t0)); /* xint = xlow - ylow*y */
		}

	      lsp[nf] = xint;         
	      xlow = xint;            
	      nf++;

	       
	      if (ip == 0)
		{
		  ip = 1;             
		  coef = f2;          
		}
	      else
		{
		  ip = 0;             
		  coef = f1;          
		}
	      ylow = Chebps (xlow, coef, NC);
	       

	    }
	    
	}

      /* Check if M roots found */

       
      if (sub (nf, M) < 0)
	{
	  for (i = 0; i < M; i++)
	    {
	      lsp[i] = old_lsp[i];    
	    }

	}
    }
};

