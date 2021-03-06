/*************************************************************************
 *
 *  BEHAVIOR:   pitch_fr6()
 *
 *  PURPOSE: Find the pitch period with 1/6 subsample resolution (closed loop).
 *
 *  DESCRIPTION:
 *        - find the normalized correlation between the target and filtered
 *          past excitation in the search range.
 *        - select the delay with maximum normalized correlation.
 *        - interpolate the normalized correlation at fractions -3/6 to 3/6
 *          with step 1/6 around the chosen delay.
 *        - The fraction which gives the maximum interpolated value is chosen.
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"


import "basic_op";
import "basic_func";




/* L_FIntERPL = Length for fractional interpolation = nb.coeff/2  */
#define L_FIntERPL 4



behavior Pitch_Fr6 (
  in  Word16 *exc,       /* (i): excitation buffer                      */
  in  Word16 xn[L_SUBFR],/* (i): target vector                          */
  in  Word16 *h1,        /* (i): impulse response of synthesis and weighting */
  in  Word16 t0_min,     /* (i): minimum value in the searched range.   */
  in  Word16 t0_max,     /* (i): maximum value in the searched range.   */
  in  Int i_subfr,       /* (i): indicator for first subframe.          */
  out Word16 pit_frac,   /* (o): chosen fraction.                       */
  out Word16 lag         /* (o): pitch period.                          */
  )
{


  /*************************************************************************
   *
   *  FUNCTION:   Norm_Corr()
   *
   *  PURPOSE: Find the normalized correlation between the target vector
   *           and the filtered past excitation.
   *
   *  DESCRIPTION:
   *     The normalized correlation is given by the correlation between the
   *     target and filtered past excitation divided by the square root of
   *     the energy of filtered excitation.
   *                   corr[k] = <x[], y_k[]>/sqrt(y_k[],y_k[])
   *     where x[] is the target vector and y_k[] is the filtered past
   *     excitation at delay k.
   *
   *************************************************************************/

  void Norm_Corr (Word16 NC_exc[], Word16 NC_xn[], Word16 NC_h[], 
		  Word16 t_min, Word16 t_max, Word16 corr_norm[])
    {
      Int i, j, k;
      Word16 corr__h, corr__l, norm__h, norm__l;
      Word32 s;

      /* Usally dynamic allocation of (L_SUBFR) */
      Word16 excf[80];
      Word16 scaling, h_fac, *s_excf, scaled_excf[80];



      k = -t_min;                                 

      /* compute the filtered excitation for the first delay t_min */

      Convolut (&NC_exc[k], NC_h, excf, L_SUBFR);

      /* scale "excf[]" to avoid overflow */

      for (j = 0; j < L_SUBFR; j++)
	{
	  scaled_excf[j] = shr (excf[j], 2);      
	}

      /* Compute 1/sqrt(energy of excf[]) */

      s = 0;                                      
      for (j = 0; j < L_SUBFR; j++)
	{
	  s = L_mac (s, excf[j], excf[j]);
	}
       
      if (L_sub (s, 67108864L) <= 0)             /* if (s <= 2^26) */
	{
	  s_excf = excf;                          
	  h_fac = 15 - 12;                        
	  scaling = 0;                            
	}
      else
	{
	  /* "excf[]" is divided by 2 */
	  s_excf = scaled_excf;                   
	  h_fac = 15 - 12 - 2;                    
	  scaling = 2;                            
	}

      /* loop for every possible period */

      for (i = t_min; i <= t_max; i++)
	{
	  /* Compute 1/sqrt(energy of excf[]) */

	  s = 0;                                  
	  for (j = 0; j < L_SUBFR; j++)
	    {
	      s = L_mac (s, s_excf[j], s_excf[j]);
	    }

	  s = Inv_sqrt (s);                       
	  L_Extract (s, &norm__h, &norm__l);

	  /* Compute correlation between xn[] and excf[] */

	  s = 0;                                   
	  for (j = 0; j < L_SUBFR; j++)
	    {
	      s = L_mac (s, NC_xn[j], s_excf[j]);
	    }
	  L_Extract (s, &corr__h, &corr__l);

	  /* Normalize correlation = correlation * (1/sqrt(energy)) */

	  s = Mpy_32 (corr__h, corr__l, norm__h, norm__l);

	  corr_norm[i] = extract_h (L_shl (s, 16));
	   

	  /* modify the filtered excitation excf[] for the next iteration */

	   
	  if (i != t_max)
	    {
	      k--;
	      for (j = L_SUBFR - 1; j > 0; j--)
		{
		  s = L_mult (NC_exc[k], NC_h[j]);
		  s = L_shl (s, h_fac);
		  s_excf[j] = add (extract_h (s), s_excf[j - 1]);  
		}
	      s_excf[0] = shr (NC_exc[k], scaling);   
	    }
	}
      return;
    }




  /*************************************************************************
   *
   *  FUNCTION:  Interpol_6()
   *
   *  PURPOSE:  Interpolating the normalized correlation with 1/6 resolution.
   *
   *************************************************************************/

#define UP_SAMP      6
#define FIR_SIZE     (UP_SAMP*L_FIntERPL+1)


  Word16 Interpol_6 (                 /* (o)  : interpolated value  */
		     Word16 *x,       /* (i)  : input vector        */
		     Word16 frac      /* (i)  : fraction            */
		     )
    {
      Int i, k;
      Word16 *x1, *x2;
      const Word16 *c1, *c2;
      Word32 s;


      /* 1/6 resolution interpolation filter  (-3 dB at 3600 Hz) */

      const Word16 inter_6[FIR_SIZE] =
      {
	29519,
	28316, 24906, 19838, 13896, 7945, 2755,
	-1127, -3459, -4304, -3969, -2899, -1561,
	-336, 534, 970, 1023, 823, 516,
	220, 0, -131, -194, -215, 0
      };


       
      if (frac < 0)
	{
	  frac = add (frac, UP_SAMP);
	  x--;
	}
      x1 = &x[0];                          
      x2 = &x[1];                          
      c1 = &inter_6[frac];                 
      c2 = &inter_6[sub (UP_SAMP, frac)];  

      s = 0;                               
      for (i = 0, k = 0; i < L_FIntERPL; i++, k += UP_SAMP)
	{
	  s = L_mac (s, x1[-i], c1[k]);
	  s = L_mac (s, x2[i], c2[k]);
	}

      return round (s);
    }






  void main(void)
    {
      Int i;
      Word16 l;
      Word16 t_min, t_max;
      Word16 max, frac;
      Word16 *corr;
      Word16 corr_int;
      Word16 corr_v[40];       /* Total length = t0_max-t0_min+1+2*L_inter */

      /* Find interval to compute normalized correlation */

      t_min = sub (t0_min, L_FIntERPL);
      t_max = add (t0_max, L_FIntERPL);

      corr = &corr_v[-t_min];                     

      /* Compute normalized correlation between target 
         and filtered excitation */

      Norm_Corr (exc, &xn[0], h1, t_min, t_max, corr);

      /* Find integer pitch */

      max = corr[t0_min];                         
      l = t0_min;                               

      for (i = t0_min + 1; i <= t0_max; i++)
	{
	   
	  if (sub (corr[i], max) >= 0)
	    {
	      max = corr[i];                      
	      l = i;                            
	    }
	}

      /* If first subframe and lag > 94 do not search fractional pitch */

        
      if ((i_subfr == 0) && (sub (l, 94) > 0))
	{
	  pit_frac = 0;                          
	  //return (lag);
	  lag = l;
	  return;
	}
      /* Test the fractions around T0 and choose the one which maximizes   */
      /* the interpolated normalized correlation.                          */

      max = Interpol_6 (&corr[l], -3);
      frac = -3;                                  

      for (i = -2; i <= 3; i++)
	{
	  corr_int = Interpol_6 (&corr[l], i);  
	   
	  if (sub (corr_int, max) > 0)
	    {
	      max = corr_int;                     
	      frac = i;                           
	    }
	}

      /* Limit the fraction value in the interval [-2,-1,0,1,2,3] */

       
      if (sub (frac, -3) == 0)
	{
	  frac = 3;                               
	  l = sub (l, 1);
	}
      pit_frac = frac;                          
    
      //return (lag);
      lag = l;
    }
};


